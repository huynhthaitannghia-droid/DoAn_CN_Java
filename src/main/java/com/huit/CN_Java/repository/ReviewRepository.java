package com.huit.CN_Java.repository;

import com.huit.CN_Java.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Lấy tất cả đánh giá của một sản phẩm (hiển thị ngay, không cần duyệt)
    List<Review> findByProductIdOrderByCreatedAtDesc(Long productId);

    // Lấy tất cả đánh giá — Admin xem toàn bộ
    List<Review> findAllByOrderByCreatedAtDesc();

    // Kiểm tra user đã đánh giá sản phẩm này chưa
    boolean existsByUserIdAndProductId(Long userId, Long productId);
}
