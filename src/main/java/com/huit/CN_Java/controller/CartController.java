package com.huit.CN_Java.controller;

import com.huit.CN_Java.dto.CheckoutDTO;
import com.huit.CN_Java.entity.Coupon;
import com.huit.CN_Java.entity.Order;
import com.huit.CN_Java.service.*;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final ProductService productService;
    private final OrderService orderService;
    private final UserService userService;

    // ==================== GIỎ HÀNG ====================

    @GetMapping("/cart")
    public String viewCart(HttpSession session, Model model) {
        model.addAttribute("cartItems", cartService.getCart(session));
        model.addAttribute("total", cartService.getTotal(session));
        return "cart/cart";
    }

    @PostMapping("/cart/add")
    public String addToCart(@RequestParam Long productId,
                            @RequestParam(defaultValue = "1") int quantity,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        try {
            cartService.addToCart(session, productService.getById(productId), quantity);
            redirectAttributes.addFlashAttribute("toast", "Đã thêm vào giỏ hàng! 🛒");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("toastError", e.getMessage());
        }
        return "redirect:/products/" + productId;
    }

    @PostMapping("/cart/update")
    public String updateCart(@RequestParam Long productId,
                             @RequestParam int quantity,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        try {
            cartService.updateQuantity(session, productId, quantity,
                    productService.getById(productId));
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("toastError", e.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/cart/remove")
    public String removeFromCart(@RequestParam Long productId,
                                 HttpSession session) {
        cartService.removeFromCart(session, productId);
        return "redirect:/cart";
    }

    // ==================== THANH TOÁN ====================

    @GetMapping("/checkout")
    public String checkoutPage(HttpSession session, Model model,
                               @AuthenticationPrincipal UserDetails userDetails) {
        if (cartService.getCart(session).isEmpty()) {
            return "redirect:/cart";
        }
        model.addAttribute("cartItems", cartService.getCart(session));
        model.addAttribute("total", cartService.getTotal(session));

        CheckoutDTO dto = new CheckoutDTO();
        try {
            var user = userService.findByEmail(userDetails.getUsername());
            if (user.getPhone() != null) {
                dto.setPhone(user.getPhone());
            }
        } catch (Exception ignored) {}

        model.addAttribute("checkoutDTO", dto);
        return "cart/checkout";
    }

    // Áp dụng coupon (AJAX)
    @PostMapping("/checkout/apply-coupon")
    @ResponseBody
    public java.util.Map<String, Object> applyCoupon(
            @RequestParam String code,
            HttpSession session) {
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        try {
            double total = cartService.getTotal(session);
            Coupon coupon = orderService.applyCoupon(code, total);
            double discount = coupon.calculateDiscount(total);
            session.setAttribute("COUPON", coupon);
            result.put("success", true);
            result.put("discount", discount);
            result.put("finalPrice", total - discount);
            result.put("message", "Áp dụng thành công! Giảm " +
                    String.format("%,.0f", discount) + "đ");
        } catch (RuntimeException e) {
            session.removeAttribute("COUPON");
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    @PostMapping("/checkout")
    public String placeOrder(@Valid @ModelAttribute CheckoutDTO checkoutDTO,
                             BindingResult result,
                             HttpSession session,
                             @AuthenticationPrincipal UserDetails userDetails,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("cartItems", cartService.getCart(session));
            model.addAttribute("total", cartService.getTotal(session));
            return "cart/checkout";
        }
        try {
            Coupon coupon = (Coupon) session.getAttribute("COUPON");
            Order order = orderService.placeOrder(
                    userDetails.getUsername(),
                    cartService.getCart(session),
                    checkoutDTO,
                    coupon
            );
            cartService.clearCart(session);
            session.removeAttribute("COUPON");
            redirectAttributes.addFlashAttribute("successMsg",
                    "Đặt hàng thành công! Mã đơn: #" + order.getId());
            return "redirect:/orders/" + order.getId();
        } catch (RuntimeException e) {
            model.addAttribute("errorMsg", e.getMessage());
            model.addAttribute("cartItems", cartService.getCart(session));
            model.addAttribute("total", cartService.getTotal(session));
            return "cart/checkout";
        }
    }

    // ==================== LỊCH SỬ ĐƠN HÀNG ====================

    @GetMapping("/orders")
    public String orderHistory(@AuthenticationPrincipal UserDetails userDetails,
                               Model model) {
        model.addAttribute("orders",
                orderService.getOrdersByUser(userDetails.getUsername()));
        return "order/history";
    }

    @GetMapping("/orders/{id}")
    public String orderDetail(@PathVariable Long id, Model model,
                              @AuthenticationPrincipal UserDetails userDetails) {
        Order order = orderService.getOrderById(id);
        if (!order.getUser().getEmail().equals(userDetails.getUsername())) {
            return "redirect:/orders";
        }
        model.addAttribute("order", order);
        return "order/detail";
    }

    @PostMapping("/orders/{id}/cancel")
    public String cancelOrder(@PathVariable Long id,
                              @AuthenticationPrincipal UserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        try {
            orderService.cancelOrder(id, userDetails.getUsername());
            redirectAttributes.addFlashAttribute("successMsg", "Đã hủy đơn hàng");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/orders/" + id;
    }
}