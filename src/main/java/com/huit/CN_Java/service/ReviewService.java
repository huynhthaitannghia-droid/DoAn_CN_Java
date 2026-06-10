package com.huit.CN_Java.service;

import com.huit.CN_Java.entity.Product;
import com.huit.CN_Java.entity.Review;
import com.huit.CN_Java.entity.Role;
import com.huit.CN_Java.entity.User;
import com.huit.CN_Java.repository.OrderRepository;
import com.huit.CN_Java.repository.ProductRepository;
import com.huit.CN_Java.repository.ReviewRepository;
import com.huit.CN_Java.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    /**
     * User gửi đánh giá:
     *   1. Admin không được đánh giá.
     *   2. Phải có đơn hàng COMPLETED chứa sản phẩm mới được đánh giá.
     *   3. Mỗi user chỉ đánh giá 1 lần / sản phẩm.
     */
    @Transactional
    public void submitReview(String userEmail, Long productId, int rating, String comment) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        if (user.getRole() == Role.ADMIN) {
            throw new IllegalStateException("Quản trị viên không được phép đánh giá sản phẩm.");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        if (!orderRepository.hasPurchasedProduct(user.getId(), productId)) {
            throw new IllegalStateException("Bạn cần mua và nhận hàng thành công sản phẩm này mới có thể đánh giá.");
        }

        if (reviewRepository.existsByUserIdAndProductId(user.getId(), productId)) {
            throw new IllegalStateException("Bạn đã đánh giá sản phẩm này rồi.");
        }

        Review review = new Review();
        review.setUser(user);
        review.setProduct(product);
        review.setRating(rating);
        review.setComment(comment);

        reviewRepository.save(review);
    }

    /** Lấy tất cả đánh giá của sản phẩm (hiển thị công khai) */
    public List<Review> getReviewsByProduct(Long productId) {
        return reviewRepository.findByProductIdOrderByCreatedAtDesc(productId);
    }

    /** Admin: lấy danh sách tất cả đánh giá */
    public List<Review> getAllReviews() {
        return reviewRepository.findAllByOrderByCreatedAtDesc();
    }

    /** Admin: phản hồi đánh giá */
    @Transactional
    public void replyReview(Long reviewId, String reply) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đánh giá"));
        review.setAdminReply(reply != null ? reply.trim() : null);
        reviewRepository.save(review);
    }

    /** Admin: xóa đánh giá có ngôn từ không phù hợp */
    @Transactional
    public void deleteReview(Long reviewId) {
        reviewRepository.deleteById(reviewId);
    }
}
