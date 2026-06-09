package com.huit.CN_Java.service;

import com.huit.CN_Java.entity.Product;
import com.huit.CN_Java.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public List<Product> getFeaturedProducts() {
        return productRepository.findByFeaturedTrueAndActiveTrue();
    }

    public List<Product> getSaleProducts() {
        return productRepository.findBySalePriceNotNullAndActiveTrue();
    }

    public List<Product> filterProducts(String name, Long categoryId,
                                        Double minPrice, Double maxPrice,
                                        String sortBy) {
        return productRepository.filterProducts(name, categoryId, minPrice, maxPrice, sortBy);
    }

    public Product getById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
    }

    public List<Product> getLowStockProducts() {
        return productRepository.findByStockLessThanAndActiveTrue(5);
    }

    public List<Product> getByCategory(Long categoryId) {
        return productRepository.findByCategoryIdAndActiveTrue(categoryId);
    }
}