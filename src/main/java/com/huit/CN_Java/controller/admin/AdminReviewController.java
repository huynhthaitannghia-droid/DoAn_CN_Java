package com.huit.CN_Java.controller.admin;

import com.huit.CN_Java.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/reviews")
@RequiredArgsConstructor
public class AdminReviewController {

    private final ReviewService reviewService;

    /** Hiển thị danh sách tất cả đánh giá */
    @GetMapping
    public String list(Model model) {
        model.addAttribute("reviews", reviewService.getAllReviews());
        return "admin/review/list";
    }

    /** Admin phản hồi đánh giá */
    @PostMapping("/{id}/reply")
    public String reply(@PathVariable Long id,
                        @RequestParam String adminReply,
                        RedirectAttributes ra) {
        reviewService.replyReview(id, adminReply);
        ra.addFlashAttribute("successMsg", "Đã lưu phản hồi.");
        return "redirect:/admin/reviews";
    }

    /** Admin xóa đánh giá có ngôn từ không phù hợp */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        reviewService.deleteReview(id);
        ra.addFlashAttribute("successMsg", "Đã xóa đánh giá.");
        return "redirect:/admin/reviews";
    }
}
