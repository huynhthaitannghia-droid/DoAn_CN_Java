package com.huit.CN_Java.service;

import com.huit.CN_Java.entity.Product;
import com.huit.CN_Java.entity.ProductImage;
import com.huit.CN_Java.repository.ProductImageRepository;
import com.huit.CN_Java.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public org.springframework.data.domain.Page<Product> searchAdmin(String keyword, Long categoryId, org.springframework.data.domain.Pageable pageable) {
        return productRepository.searchAdmin(keyword != null ? keyword : "", categoryId, pageable);
    }

    public List<Product> getLowStockProducts(int threshold) {
        return productRepository.findLowStock(threshold);
    }

    public List<Product> getTopSellingProducts(org.springframework.data.domain.Pageable pageable) {
        return productRepository.findTopSelling(pageable);
    }

    public void toggleActive(Long id) {
        Product product = findByIdOrThrow(id);
        product.setActive(!product.isActive());
        productRepository.save(product);
    }

    public Product save(Product product) {
        return productRepository.save(product);
    }

    public void deleteById(Long id) {
        productRepository.deleteById(id);
    }

    /**
     * Lưu danh sách ảnh mới cho sản phẩm.
     * primaryIndex: vị trí (0-based) trong list là ảnh chính.
     */
    @Transactional
    public void saveImages(Product product, List<String> imagePaths, int primaryIndex) {
        if (imagePaths == null || imagePaths.isEmpty()) return;
        // Bỏ flag primary của các ảnh cũ nếu có ảnh mới primary
        List<ProductImage> existing = productImageRepository.findByProduct(product);
        if (!existing.isEmpty()) {
            existing.forEach(img -> img.setPrimary(false));
            productImageRepository.saveAll(existing);
        }
        for (int i = 0; i < imagePaths.size(); i++) {
            ProductImage img = new ProductImage();
            img.setImagePath(imagePaths.get(i));
            img.setProduct(product);
            img.setPrimary(i == primaryIndex);
            productImageRepository.save(img);
        }
    }

    /** Giữ tương thích với code cũ (mặc định ảnh đầu tiên là primary) */
    public void saveImages(Product product, List<String> imagePaths) {
        saveImages(product, imagePaths, 0);
    }

    public void deleteImages(Product product) {
        List<ProductImage> oldImages = productImageRepository.findByProduct(product);
        productImageRepository.deleteAll(oldImages);
    }

    public void deleteImagesByIds(List<Long> imageIds) {
        if (imageIds != null && !imageIds.isEmpty()) {
            productImageRepository.deleteAllById(imageIds);
        }
    }

    /**
     * Đặt một ảnh cụ thể làm ảnh chính, bỏ primary của các ảnh khác cùng sản phẩm.
     */
    @Transactional
    public void setPrimaryImage(Long productId, Long imageId) {
        List<ProductImage> images = productImageRepository.findByProductId(productId);
        for (ProductImage img : images) {
            img.setPrimary(img.getId().equals(imageId));
        }
        productImageRepository.saveAll(images);
    }

    /**
     * Bỏ flag primary của tất cả ảnh thuộc sản phẩm (trừ excludeId nếu != null).
     */
    @Transactional
    public void clearOtherPrimary(Long productId, Long excludeId) {
        List<ProductImage> images = productImageRepository.findByProductId(productId);
        for (ProductImage img : images) {
            if (excludeId == null || !img.getId().equals(excludeId)) {
                img.setPrimary(false);
            }
        }
        productImageRepository.saveAll(images);
    }

    public Product findByIdOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
    }
}