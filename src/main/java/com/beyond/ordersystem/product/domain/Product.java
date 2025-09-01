package com.beyond.ordersystem.product.domain;

import com.beyond.ordersystem.common.domain.BaseTimeEntity;
import com.beyond.ordersystem.member.domain.Member;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class Product extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String name;
    @Column(nullable = false)
    private String category;
    @Column(nullable = false)
    private int price;
    @Column(nullable = false)
    @Setter
    private int stockQuantity;
    @JoinColumn(name = "member_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;
    @Setter
    private String productImgae;

    public void updateProduct(String name, String category, int price, int stockQuantity) {
        this.name = name;
        this.category = category;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }

    public boolean decreaseQuantity(int orderQuantity) {
        if(this.stockQuantity < orderQuantity) {
            return false;
        } else {
            this.stockQuantity -= orderQuantity;
            return true;
        }
    }

    public void increaseQuantity(int orderQuantity) {
        this.stockQuantity += orderQuantity;
    }

}
