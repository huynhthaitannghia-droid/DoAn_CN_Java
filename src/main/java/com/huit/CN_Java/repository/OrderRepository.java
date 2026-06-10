package com.huit.CN_Java.repository;

import com.huit.CN_Java.entity.Order;
import com.huit.CN_Java.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserOrderByCreatedAtDesc(User user);

    @Query("SELECT COALESCE(SUM(o.finalPrice),0) FROM Order o WHERE o.status='COMPLETED'")
    Double getTotalRevenue();

    long countByStatus(Order.OrderStatus status);

    @Query("SELECT o FROM Order o WHERE " +
            "(:keyword='' OR CAST(o.id AS string) LIKE %:keyword% OR o.phone LIKE %:keyword%) " +
            "AND (:status IS NULL OR o.status=:status)")
    Page<Order> searchAdmin(@Param("keyword") String keyword,
                            @Param("status") Order.OrderStatus status, Pageable pageable);

    @Query("SELECT o FROM Order o ORDER BY o.createdAt DESC")
    List<Order> findTopN(Pageable pageable);

    @Query("SELECT COALESCE(SUM(o.finalPrice),0) FROM Order o WHERE o.status='COMPLETED' AND YEAR(o.createdAt)=:y AND MONTH(o.createdAt)=:m")
    Double getRevenueByMonth(@Param("y") int year, @Param("m") int month);

    @Query("SELECT COALESCE(SUM(o.finalPrice),0) FROM Order o WHERE o.status='COMPLETED' AND DATE(o.createdAt)=:date")
    Double getRevenueByDate(@Param("date") LocalDate date);

    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Kiểm tra user đã từng mua sản phẩm này và đơn hàng đã COMPLETED chưa.
     */
    @Query("SELECT COUNT(oi) > 0 FROM OrderItem oi " +
           "WHERE oi.order.user.id = :userId " +
           "AND oi.product.id = :productId " +
           "AND oi.order.status = com.huit.CN_Java.entity.Order$OrderStatus.COMPLETED")
    boolean hasPurchasedProduct(@Param("userId") Long userId,
                                @Param("productId") Long productId);
}
