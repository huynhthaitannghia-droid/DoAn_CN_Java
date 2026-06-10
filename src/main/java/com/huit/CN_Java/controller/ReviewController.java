package com.huit.CN_Java.controller;

import com.huit.CN_Java.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * POST /products/{id}/review
     * User gửi đánh giá từ trang chi tiết sản phẩm.
     */
    @PostMapping("/products/{id}/review")
    public String submitReview(
            @PathVariable Long id,
            @RequestParam int rating,
            @RequestParam String comment,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes ra) {

        try {
            reviewService.submitReview(userDetails.getUsername(), id, rating, comment);
            ra.addFlashAttribute("toast", "Cảm ơn bạn đã đánh giá! Đánh giá của bạn đã được ghi nhận.");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("toastError", e.getMessage());
        } catch (Exception e) {
            ra.addFlashAttribute("toastError", "Có lỗi xảy ra, vui lòng thử lại.");
        }

        return "redirect:/products/" + id;
    }
}