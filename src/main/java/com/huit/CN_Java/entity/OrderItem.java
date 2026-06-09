package com.huit.CN_Java.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    @Min(value = 1)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false)
    private Double unitPrice; // Lưu giá tại thời điểm mua (tránh bị thay đổi sau)

    public Double getSubtotal() {
        return unitPrice * quantity;
    }
}