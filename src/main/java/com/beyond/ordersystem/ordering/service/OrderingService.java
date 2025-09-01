package com.beyond.ordersystem.ordering.service;

import com.beyond.ordersystem.common.service.SseAlarmService;
//import com.beyond.ordersystem.common.service.StockRabbitMqService;
import com.beyond.ordersystem.member.domain.Member;
import com.beyond.ordersystem.member.repository.MemberRepository;
import com.beyond.ordersystem.ordering.domain.OrderDetail;
import com.beyond.ordersystem.ordering.domain.OrderStatus;
import com.beyond.ordersystem.ordering.domain.Ordering;
import com.beyond.ordersystem.ordering.dto.OrderCreateDto;
import com.beyond.ordersystem.ordering.dto.OrderDetailDto;
import com.beyond.ordersystem.ordering.dto.OrderListResDto;
import com.beyond.ordersystem.ordering.repository.OrderingDetailRepository;
import com.beyond.ordersystem.ordering.repository.OrderingRepository;
import com.beyond.ordersystem.product.domain.Product;
import com.beyond.ordersystem.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderingService {
    private final OrderingRepository orderingRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final OrderingDetailRepository orderingDetailRepository;
    private final SseAlarmService sseAlarmService;

    // 주문 생성
//    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Long createOrdering(List<OrderCreateDto> dtos) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("없는 사용자입니다."));

        // 주문 먼저 저장하고
        Ordering ordering = Ordering.builder().orderStatus(OrderStatus.ORDERED).member(member).build();
        orderingRepository.save(ordering);

        // 저장한 주문번호 가져와서, OrderingDetail 저장
        // product id 뽑아서 product 객체 추출
        for(OrderCreateDto dto : dtos) {
            Product product = productRepository.findById(dto.getProductId()).orElseThrow(() -> new EntityNotFoundException("없는 상품입니다."));
            int quantity = dto.getProductCount();
            OrderDetail orderDetail = OrderDetail.builder().product(product).quantity(quantity).ordering(ordering).build();

            // @OneToMany + Cascade 조합으로 따로 save 없이 저장될 수 있게
            ordering.getOrderDetailList().add(orderDetail);

            // 1. 동시에 접근하는 상황에서 update값의 정합성이 깨지고 갱신이상(lost update)가 발생할 수 있다. (스프링 2점대)
            // 2. Spring 버전이나 MySQL 버전에 따라 JPA에서 강제 에러(데드락)를 유발시켜 대부분의 요청실패 발생 (스프링 3점대)

            // 재고 관리
            boolean check = product.decreaseQuantity(quantity); // 조건은 여기서 해결하는게 나을 듯
            if(!check) {
                // 모든 임시 저장 사항들을 롤백 처리
                throw new IllegalArgumentException("재고가 부족합니다.");
            }
        }

        // 주문성공 시 admin 유저에게 알림메시지 전송
        sseAlarmService.publishMessage("admin@naver.com", email, ordering.getId());

        return ordering.getId();
    }

    // 주문 목록 조회
    public List<OrderListResDto> getOrderingList() {
        List<OrderListResDto> orderListResDtoList = new ArrayList<>();
        List<Ordering> orderingList = orderingRepository.findAll();

        // Ordering (id, orderStatus), Member(memberEmail)
        // OrderDetail (detailId, productName, productCount)
        for(Ordering ordering : orderingList) {
            List<OrderDetailDto> orderDetailDtoList = new ArrayList<>();

            List<OrderDetail> orderDetailList = orderingDetailRepository.findByOrdering(ordering);
            for(OrderDetail orderDetail : orderDetailList) {
                orderDetailDtoList.add(OrderDetailDto.fromEntity(orderDetail));
            }
            OrderListResDto orderListResDto = OrderListResDto.fromEntity(ordering, orderDetailDtoList);
            orderListResDtoList.add(orderListResDto);
        }

        return orderListResDtoList;
    }

    // 나의 주문 목록 조회
    public List<OrderListResDto> getMyOrderingList() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        List<Ordering> orderingList = orderingRepository.findByMemberEmail(email);

        List<OrderListResDto> orderListResDtoList = new ArrayList<>();

        for(Ordering ordering : orderingList) {
            List<OrderDetailDto> orderDetailDtoList = new ArrayList<>();

            List<OrderDetail> orderDetailList = orderingDetailRepository.findByOrdering(ordering);
            for(OrderDetail orderDetail : orderDetailList) {
                orderDetailDtoList.add(OrderDetailDto.fromEntity(orderDetail));
            }
            OrderListResDto orderListResDto = OrderListResDto.fromEntity(ordering, orderDetailDtoList);
            orderListResDtoList.add(orderListResDto);
        }

        return orderListResDtoList;
    }

    // 주문 취소
    public Ordering cancel(Long id) {
        // Ordering의 DB 상태값 변경
        Ordering ordering = orderingRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("없는 주문입니다."));
        ordering.setOrderStatus(OrderStatus.CANCELED);

        for(OrderDetail orderDetail : ordering.getOrderDetailList()) {
            Long productId = orderDetail.getProduct().getId();
            int quantity = orderDetail.getQuantity();

            // rabbitmq에 재고 증가 메시지 발행 -> 굳이 필요없을 것으로 보임
            // 바로 RDB에 재고 업데이트
            Product product = productRepository.findById(productId).orElseThrow(() -> new EntityNotFoundException("없는 상품입니다."));
            product.increaseQuantity(quantity);
        }

        return ordering;
    }

}
