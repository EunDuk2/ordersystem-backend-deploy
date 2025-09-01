package com.beyond.ordersystem.ordering.dto;

import com.beyond.ordersystem.ordering.domain.OrderDetail;
import com.beyond.ordersystem.ordering.domain.OrderStatus;
import com.beyond.ordersystem.ordering.domain.Ordering;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderListResDto {
    private Long orderingId;
    private OrderStatus orderStatus;
    private String memberEmail;
    @Builder.Default
    private List<OrderDetailDto> orderDetails = new ArrayList<>();

    public static OrderListResDto fromEntity(Ordering ordering, List<OrderDetailDto> orderDetailDtoList) {
        return OrderListResDto.builder()
                .orderingId(ordering.getId())
                .orderStatus(ordering.getOrderStatus())
                .memberEmail(ordering.getMember().getEmail())
                .orderDetails(orderDetailDtoList)
                .build();
    }
}
