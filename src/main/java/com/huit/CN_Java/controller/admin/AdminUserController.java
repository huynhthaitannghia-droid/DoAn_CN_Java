package com.huit.CN_Java.controller.admin;

import com.huit.CN_Java.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "") String keyword,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        model.addAttribute("users", userService.searchAdmin(keyword, pageable));
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentPage", page);
        return "admin/user/list";
    }

    @PostMapping("/{id}/toggle-lock")
    public String toggleLock(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        userService.toggleLock(id);
        redirectAttributes.addFlashAttribute("successMsg", "Da cap nhat tai khoan");
        return "redirect:/admin/users";
    }
}
