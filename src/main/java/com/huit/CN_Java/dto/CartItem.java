package com.huit.CN_Java.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {
    private Long productId;
    private String productName;
    private String thumbnail;
    private Double price;
    private Integer quantity;

    public Double getSubtotal() {
        return price * quantity;
    }
}