package com.huit.CN_Java.service;

import com.huit.CN_Java.dto.CartItem;
import com.huit.CN_Java.dto.CheckoutDTO;
import com.huit.CN_Java.entity.*;
import com.huit.CN_Java.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CouponRepository couponRepository;
    private final UserService userService;

    // Áp dụng coupon
    public Coupon applyCoupon(String code, double total) {
        Coupon coupon = couponRepository.findByCodeAndActiveTrue(code)
                .orElseThrow(() -> new RuntimeException("Mã giảm giá không hợp lệ"));
        if (!coupon.isValid(total)) {
            throw new RuntimeException("Mã không áp dụng được " +
                    "(đơn tối thiểu: " +
                    String.format("%,.0f", coupon.getMinOrderValue()) + "đ)");
        }
        return coupon;
    }

    // Đặt hàng
    @Transactional
    public Order placeOrder(String userEmail, List<CartItem> cartItems,
                            CheckoutDTO dto, Coupon coupon) {
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Giỏ hàng trống");
        }

        User user = userService.findByEmail(userEmail);
        Order order = new Order();
        order.setUser(user);
        order.setShippingAddress(dto.getShippingAddress());
        order.setPhone(dto.getPhone());
        order.setNote(dto.getNote());
        order.setPaymentMethod(Order.PaymentMethod.valueOf(dto.getPaymentMethod()));

        double total = 0;

        for (CartItem cartItem : cartItems) {
            Product product = productRepository.findById(cartItem.getProductId())
                    .orElseThrow(() -> new RuntimeException(
                            "Sản phẩm không tồn tại: " + cartItem.getProductName()));

            // Kiểm tra tồn kho lần cuối trước khi đặt
            if (product.getStock() < cartItem.getQuantity()) {
                throw new RuntimeException("Sản phẩm \"" + product.getName() +
                        "\" chỉ còn " + product.getStock() + " trong kho");
            }

            // Trừ tồn kho
            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepository.save(product);

            // Tạo order item
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(cartItem.getQuantity());
            item.setUnitPrice(cartItem.getPrice());
            order.getItems().add(item);

            total += cartItem.getSubtotal();
        }

        order.setTotalPrice(total);

        // Áp dụng coupon nếu có
        if (coupon != null) {
            double discount = coupon.calculateDiscount(total);
            order.setCoupon(coupon);
            order.setDiscountAmount(discount);
            order.setFinalPrice(total - discount);

            // Tăng số lần dùng coupon
            coupon.setUsedCount(coupon.getUsedCount() + 1);
            couponRepository.save(coupon);
        } else {
            order.setDiscountAmount(0.0);
            order.setFinalPrice(total);
        }

        order.setStatus(Order.OrderStatus.PENDING);
        return orderRepository.save(order);
    }

    // Lịch sử đơn hàng
    public List<Order> getOrdersByUser(String email) {
        User user = userService.findByEmail(email);
        return orderRepository.findByUserOrderByCreatedAtDesc(user);
    }

    // Chi tiết đơn
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
    }

    // Hủy đơn
    @Transactional
    public void cancelOrder(Long orderId, String userEmail) {
        Order order = getOrderById(orderId);

        if (!order.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("Bạn không có quyền hủy đơn này");
        }
        if (!order.isCancellable()) {
            throw new RuntimeException("Chỉ có thể hủy đơn đang chờ xác nhận");
        }

        // Hoàn lại tồn kho
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantity());
            productRepository.save(product);
        }

        order.setStatus(Order.OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    // --- CÁC HÀM BỔ SUNG CHO ADMIN ---
    public Double getTotalRevenue() {
        return orderRepository.getTotalRevenue();
    }

    public long countAllOrders() {
        return orderRepository.count();
    }

    public long countByStatus(com.huit.CN_Java.entity.Order.OrderStatus status) {
        return orderRepository.countByStatus(status);
    }

    public org.springframework.data.domain.Page<com.huit.CN_Java.entity.Order> searchAdmin(String keyword, String status, org.springframework.data.domain.Pageable pageable) {
        com.huit.CN_Java.entity.Order.OrderStatus orderStatus = null;
        if (status != null && !status.isBlank()) {
            orderStatus = com.huit.CN_Java.entity.Order.OrderStatus.valueOf(status);
        }
        return orderRepository.searchAdmin(keyword != null ? keyword : "", orderStatus, pageable);
    }

    public java.util.List<com.huit.CN_Java.entity.Order> getRecentOrders(org.springframework.data.domain.Pageable pageable) {
        return orderRepository.findTopN(pageable);
    }

    public Double getRevenueByDay(java.time.LocalDate date) {
        return orderRepository.getRevenueByDate(date);
    }

    public void updateStatus(Long id, String statusStr) {
        com.huit.CN_Java.entity.Order order = findByIdOrThrow(id);
        com.huit.CN_Java.entity.Order.OrderStatus newStatus;
        try {
            newStatus = com.huit.CN_Java.entity.Order.OrderStatus.valueOf(statusStr);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Trạng thái không hợp lệ: " + statusStr);
        }

        if (!order.getStatus().nextAllowed().contains(newStatus)) {
            throw new RuntimeException(
                "Không thể chuyển từ \"" + order.getStatus().getDescription() +
                "\" sang \"" + newStatus.getDescription() + "\"");
        }

        order.setStatus(newStatus);
        orderRepository.save(order);
    }

    public com.huit.CN_Java.entity.Order findByIdOrThrow(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
    }
}
