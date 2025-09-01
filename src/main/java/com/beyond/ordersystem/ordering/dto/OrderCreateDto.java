package com.beyond.ordersystem.ordering.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderCreateDto {
    @NotNull(message = "상품 id가 비어있습니다.")
    private Long productId;
    @NotNull(message = "상품 개수가 비어있습니다.")
    private int productCount;
}



//import java.util.ArrayList;
//import java.util.List;
//@Data
//@AllArgsConstructor
//@NoArgsConstructor
//@Builder
//public class OrderCreateDto {
//    @Builder.Default
//    @Valid
//    private List<SingleOrder> detail = new ArrayList<>();
//    private Long storedId;
//    private String payment;
//
//    @Data
//    @AllArgsConstructor
//    @NoArgsConstructor
//    public static class SingleOrder {
//        @NotNull(message = "상품 id가 비어있습니다.")
//        private Long productId;
//        @NotNull(message = "상품 개수가 비어있습니다.")
//        private int productCount;
//    }
//}

/*
{
    "detail": [
        {
            "productId" : 1,
            "productCount" : 3
        },
        {
            "productId" : 2,
            "productCount" : 4
        }
    ],
    "storedId" : 1,
    "payment" : "kakao"
}
 */