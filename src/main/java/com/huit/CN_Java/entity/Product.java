package com.huit.CN_Java.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    @Min(value = 0, message = "Giá không được âm")
    private Double price;

    @Column(name = "sale_price")
    private Double salePrice;

    @Column(nullable = false)
    @Min(value = 0, message = "Số lượng không được âm")
    private Integer stock = 0;

    private boolean active = true;

    @Column(name = "is_featured")
    private boolean featured = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Review> reviews = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Helper: lấy ảnh đại diện đầu tiên
    public String getThumbnail() {
        return images.isEmpty() ? "/images/default.png" : images.get(0).getImagePath();
    }

    // Helper: giá hiển thị (ưu tiên sale price)
    public Double getDisplayPrice() {
        return (salePrice != null && salePrice > 0) ? salePrice : price;
    }
}