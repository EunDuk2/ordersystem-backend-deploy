package com.beyond.ordersystem.product.dto;

import com.beyond.ordersystem.member.domain.Member;
import com.beyond.ordersystem.product.domain.Product;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductUpdateDto {
    @NotEmpty(message = "이름이 비어있습니다.")
    private String name;
    @NotEmpty(message = "카테고리가 비어있습니다.")
    private String category;
    @NotNull(message = "가격이 비어있습니다.")
    private int price;
    @NotNull(message = "수량이 비어있습니다.")
    private int stockQuantity;
    private MultipartFile productImage;
}
