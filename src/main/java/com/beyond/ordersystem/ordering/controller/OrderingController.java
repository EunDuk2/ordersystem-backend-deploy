package com.beyond.ordersystem.ordering.controller;

import com.beyond.ordersystem.common.dto.CommonSuccessDto;
import com.beyond.ordersystem.ordering.domain.Ordering;
import com.beyond.ordersystem.ordering.dto.OrderCreateDto;
import com.beyond.ordersystem.ordering.dto.OrderListResDto;
import com.beyond.ordersystem.ordering.service.OrderingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ordering")
public class OrderingController {
    private final OrderingService orderingService;

    // 주문 생성
    @PostMapping("/create")
    public ResponseEntity<?> createOrdering(@RequestBody @Valid List<OrderCreateDto> dto) {

        Long id = orderingService.createOrdering(dto);

        return new ResponseEntity<>(new CommonSuccessDto(id, HttpStatus.OK.value(), "주문 성공"), HttpStatus.CREATED);
    }

    // 주문 목록 조회
    @GetMapping("/list")
    public ResponseEntity<?> getOrderingList() {

        List<OrderListResDto> orderListResDtoList = orderingService.getOrderingList();

        return new ResponseEntity<>(new CommonSuccessDto(orderListResDtoList, HttpStatus.OK.value(), "주문목록 조회 성공"), HttpStatus.OK);
    }

    // 나의 주문 목록 조회
    @GetMapping("/myorders")
    public ResponseEntity<?> getMyOrderingList() {

        List<OrderListResDto> orderListResDtoList = orderingService.getMyOrderingList();

        return new ResponseEntity<>(new CommonSuccessDto(orderListResDtoList, HttpStatus.OK.value(), "나의 주문목록 조회 성공"), HttpStatus.OK);
    }

    // 주문 취소
    @DeleteMapping("/cancel/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> orderCancel(@PathVariable Long id) {
        Ordering ordering = orderingService.cancel(id);
        return new ResponseEntity<>(new CommonSuccessDto(ordering.getId(), HttpStatus.OK.value(), "주문취소 성공"), HttpStatus.OK);
    }

}
