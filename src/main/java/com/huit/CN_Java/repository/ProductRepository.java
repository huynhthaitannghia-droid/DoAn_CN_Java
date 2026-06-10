package com.huit.CN_Java.repository;

import com.huit.CN_Java.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Tìm theo danh mục
    List<Product> findByCategoryIdAndActiveTrue(Long categoryId);

    // Tìm sản phẩm nổi bật
    List<Product> findByFeaturedTrueAndActiveTrue();

    // Tìm sản phẩm có sale
    List<Product> findBySalePriceNotNullAndActiveTrue();

    // Tìm kiếm theo tên
    List<Product> findByNameContainingIgnoreCaseAndActiveTrue(String name);

    // Lọc theo tên + danh mục + khoảng giá (dùng JPQL)
    @Query("""
        SELECT p FROM Product p
        WHERE p.active = true
        AND (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')))
        AND (:categoryId IS NULL OR p.category.id = :categoryId)
        AND (:minPrice IS NULL OR p.price >= :minPrice)
        AND (:maxPrice IS NULL OR p.price <= :maxPrice)
        ORDER BY
            CASE WHEN :sortBy = 'price_asc'  THEN p.price END ASC,
            CASE WHEN :sortBy = 'price_desc' THEN p.price END DESC,
            CASE WHEN :sortBy = 'newest'     THEN p.createdAt END DESC,
            p.id DESC
    """)
    List<Product> filterProducts(
            @Param("name") String name,
            @Param("categoryId") Long categoryId,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("sortBy") String sortBy
    );

    // Cảnh báo sắp hết hàng cho Admin
    List<Product> findByStockLessThanAndActiveTrue(int threshold);

    @Query("SELECT p FROM Product p WHERE (:keyword='' OR LOWER(p.name) LIKE LOWER(CONCAT('%',:keyword,'%'))) AND (:categoryId IS NULL OR p.category.id=:categoryId)")
    Page<Product> searchAdmin(@Param("keyword") String keyword,
                              @Param("categoryId") Long categoryId, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.stock < :threshold ORDER BY p.stock ASC")
    List<Product> findLowStock(@Param("threshold") int threshold);

    @Query("SELECT oi.product FROM OrderItem oi GROUP BY oi.product ORDER BY SUM(oi.quantity) DESC")
    List<Product> findTopSelling(Pageable pageable);
}
