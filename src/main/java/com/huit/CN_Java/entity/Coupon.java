package com.huit.CN_Java.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "coupons")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false)
    private DiscountType discountType; // PERCENT hoặc FIXED

    @Column(name = "discount_value", nullable = false)
    @Min(value = 0, message = "Giá trị giảm không được âm")
    private Double discountValue;

    @Column(name = "min_order_value")
    private Double minOrderValue = 0.0; // Điều kiện đơn tối thiểu

    @Column(name = "max_uses")
    private Integer maxUses;

    @Column(name = "used_count")
    private Integer usedCount = 0;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    private boolean active = true;

    public enum DiscountType {
        PERCENT, FIXED
    }

    // Helper: tính số tiền được giảm
    public double calculateDiscount(double orderTotal) {
        if (discountType == DiscountType.PERCENT) {
            return orderTotal * discountValue / 100;
        }
        return discountValue;
    }

    // Helper: kiểm tra coupon còn dùng được không
    public boolean isValid(double orderTotal) {
        if (!active) return false;
        if (expiryDate != null && expiryDate.isBefore(LocalDateTime.now())) return false;
        if (maxUses != null && usedCount >= maxUses) return false;
        if (orderTotal < minOrderValue) return false;
        return true;
    }
}