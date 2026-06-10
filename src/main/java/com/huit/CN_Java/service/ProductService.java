package com.huit.CN_Java.service;

import com.huit.CN_Java.entity.Product;
import com.huit.CN_Java.repository.ProductImageRepository;
import com.huit.CN_Java.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;

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

    // --- CÁC HÀM BỔ SUNG CHO ADMIN ---
    public org.springframework.data.domain.Page<com.huit.CN_Java.entity.Product> searchAdmin(String keyword, Long categoryId, org.springframework.data.domain.Pageable pageable) {
        return productRepository.searchAdmin(keyword != null ? keyword : "", categoryId, pageable);
    }

    public java.util.List<com.huit.CN_Java.entity.Product> getLowStockProducts(int threshold) {
        return productRepository.findLowStock(threshold);
    }

    public java.util.List<com.huit.CN_Java.entity.Product> getTopSellingProducts(org.springframework.data.domain.Pageable pageable) {
        return productRepository.findTopSelling(pageable);
    }

    public void toggleActive(Long id) {
        com.huit.CN_Java.entity.Product product = findByIdOrThrow(id);
        product.setActive(!product.isActive());
        productRepository.save(product);
    }

    public com.huit.CN_Java.entity.Product save(com.huit.CN_Java.entity.Product product) {
        return productRepository.save(product);
    }

    public void deleteById(Long id) {
        productRepository.deleteById(id);
    }

    public void saveImages(com.huit.CN_Java.entity.Product product, java.util.List<String> imagePaths) {
        if (imagePaths != null && !imagePaths.isEmpty()) {
            for (int i = 0; i < imagePaths.size(); i++) {
                com.huit.CN_Java.entity.ProductImage img = new com.huit.CN_Java.entity.ProductImage();
                img.setImagePath(imagePaths.get(i));
                img.setProduct(product);
                img.setPrimary(i == 0);
                productImageRepository.save(img);
            }
        }
    }

    public void deleteImages(com.huit.CN_Java.entity.Product product) {
        java.util.List<com.huit.CN_Java.entity.ProductImage> oldImages = productImageRepository.findByProduct(product);
        productImageRepository.deleteAll(oldImages);
    }

    public void deleteImagesByIds(java.util.List<Long> imageIds) {
        if (imageIds != null && !imageIds.isEmpty()) {
            productImageRepository.deleteAllById(imageIds);
        }
    }

    public com.huit.CN_Java.entity.Product findByIdOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

    }
}
