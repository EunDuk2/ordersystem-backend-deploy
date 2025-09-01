package com.beyond.ordersystem.product.controller;

import com.beyond.ordersystem.common.dto.CommonSuccessDto;
import com.beyond.ordersystem.product.domain.Product;
import com.beyond.ordersystem.product.dto.ProductCreateDto;
import com.beyond.ordersystem.product.dto.ProductResDto;
import com.beyond.ordersystem.product.dto.ProductSearchDto;
import com.beyond.ordersystem.product.dto.ProductUpdateDto;
import com.beyond.ordersystem.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/product")
public class ProductController {
    private final ProductService productService;

    // 상품 등록
    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createProduct(@ModelAttribute @Valid ProductCreateDto dto) {
        Long id = productService.createProduct(dto);
        return new ResponseEntity<>(new CommonSuccessDto(id, HttpStatus.CREATED.value(), "상품등록 성공"), HttpStatus.CREATED);
    }

    // 상품목록 조회
    @GetMapping("/list")
    public ResponseEntity<?> getProductList(@PageableDefault(size = 5, sort="id", direction = Sort.Direction.DESC)Pageable pageable, ProductSearchDto dto) {
        Page<ProductResDto> productList = productService.getProductList(pageable, dto);
        return new ResponseEntity<>(new CommonSuccessDto(productList, HttpStatus.OK.value(), "상풍목록 조회 성공"), HttpStatus.OK);
    }

    // 상품상세 조회 (-> 캐싱처리 고려)
    @GetMapping("/detail/{inputId}")
    public ResponseEntity<?> getProductDetail(@PathVariable Long inputId) {
        ProductResDto productResDto = productService.getProductDetail(inputId);
        return new ResponseEntity<>(new CommonSuccessDto(productResDto, HttpStatus.OK.value(), "상품상세 조회 성공"), HttpStatus.OK);
    }

    // 상품 수정
    @PutMapping("/update/{inputId}")
    public ResponseEntity<?> updateProduct(@PathVariable Long inputId, @ModelAttribute @Valid ProductUpdateDto dto) {
        Long id = productService.updateProduct(inputId, dto);
        return new ResponseEntity<>(new CommonSuccessDto(id, HttpStatus.OK.value(), "상품 수정 성공"), HttpStatus.OK);
    }


}
