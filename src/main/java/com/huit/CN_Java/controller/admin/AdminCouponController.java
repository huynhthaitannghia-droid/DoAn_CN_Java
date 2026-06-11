package com.huit.CN_Java.controller.admin;

import com.huit.CN_Java.entity.Coupon;
import com.huit.CN_Java.service.CouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/coupons")
@RequiredArgsConstructor
public class AdminCouponController {

    private final CouponService couponService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("coupons", couponService.findAll());
        model.addAttribute("newCoupon", new Coupon());
        return "admin/coupon/list";
    }

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("newCoupon") Coupon coupon,
                         BindingResult result,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("coupons", couponService.findAll());
            model.addAttribute("showCreateModal", true);
            return "admin/coupon/list";
        }
        if (coupon.getUsedCount() == null) coupon.setUsedCount(0);
        couponService.save(coupon);
        redirectAttributes.addFlashAttribute("successMsg", "Đã thêm mã giảm giá \"" + coupon.getCode() + "\"");
        return "redirect:/admin/coupons";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("coupon", couponService.findByIdOrThrow(id));
        model.addAttribute("coupons", couponService.findAll());
        model.addAttribute("showEditModal", true);
        model.addAttribute("editId", id);
        return "admin/coupon/list";
    }

    @PostMapping("/{id}/edit")
    public String edit(@PathVariable Long id,
                       @Valid @ModelAttribute Coupon form,
                       BindingResult result,
                       RedirectAttributes redirectAttributes,
                       Model model) {
        if (result.hasErrors()) {
            model.addAttribute("coupons", couponService.findAll());
            model.addAttribute("showEditModal", true);
            model.addAttribute("editId", id);
            model.addAttribute("coupon", form);
            return "admin/coupon/list";
        }
        Coupon existing = couponService.findByIdOrThrow(id);
        existing.setCode(form.getCode().toUpperCase());
        existing.setDiscountType(form.getDiscountType());
        existing.setDiscountValue(form.getDiscountValue());
        existing.setMinOrderValue(form.getMinOrderValue() != null ? form.getMinOrderValue() : 0.0);
        existing.setMaxUses(form.getMaxUses());
        existing.setExpiryDate(form.getExpiryDate());
        existing.setActive(form.isActive());
        couponService.save(existing);
        redirectAttributes.addFlashAttribute("successMsg", "Đã cập nhật mã \"" + existing.getCode() + "\"");
        return "redirect:/admin/coupons";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Coupon coupon = couponService.findByIdOrThrow(id);
        couponService.deleteById(id);
        redirectAttributes.addFlashAttribute("successMsg", "Đã xóa mã \"" + coupon.getCode() + "\"");
        return "redirect:/admin/coupons";
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        couponService.toggleActive(id);
        redirectAttributes.addFlashAttribute("successMsg", "Đã thay đổi trạng thái mã giảm giá");
        return "redirect:/admin/coupons";
    }
}