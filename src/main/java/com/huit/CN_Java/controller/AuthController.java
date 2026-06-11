package com.huit.CN_Java.controller;

import com.huit.CN_Java.dto.RegisterDTO;
import com.huit.CN_Java.service.PasswordResetService;
import com.huit.CN_Java.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final PasswordResetService passwordResetService;

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                            @RequestParam(required = false) String logout,
                            Model model) {
        if (error != null) model.addAttribute("errorMsg", "Email hoặc mật khẩu không đúng");
        if (logout != null) model.addAttribute("logoutMsg", "Đăng xuất thành công");
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerDTO", new RegisterDTO());
        return "auth/register";
    }

    @PostMapping("/register")
    public String doRegister(@Valid @ModelAttribute RegisterDTO registerDTO,
                             BindingResult result,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "auth/register";
        }
        try {
            userService.register(registerDTO);
            redirectAttributes.addFlashAttribute("successMsg", "Đăng ký thành công! Hãy đăng nhập.");
            return "redirect:/login";
        } catch (RuntimeException e) {
            model.addAttribute("errorMsg", e.getMessage());
            return "auth/register";
        }
    }

    // ==================== QUÊN MẬT KHẨU ====================

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String handleForgotPassword(@RequestParam String email,
                                       HttpServletRequest request,
                                       RedirectAttributes redirectAttributes) {
        try {
            String baseUrl = request.getScheme() + "://" + request.getServerName()
                             + ":" + request.getServerPort();
            passwordResetService.sendResetLink(email, baseUrl);
            redirectAttributes.addFlashAttribute("successMsg",
                    "Link đặt lại mật khẩu đã được gửi vào email của bạn. Vui lòng kiểm tra hộp thư (kể cả Spam).");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam String token, Model model,
                                    RedirectAttributes redirectAttributes) {
        if (!passwordResetService.isValidToken(token)) {
            redirectAttributes.addFlashAttribute("errorMsg",
                    "Link đặt lại mật khẩu không hợp lệ hoặc đã hết hạn.");
            return "redirect:/forgot-password";
        }
        model.addAttribute("token", token);
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String handleResetPassword(@RequestParam String token,
                                      @RequestParam String password,
                                      @RequestParam String confirmPassword,
                                      RedirectAttributes redirectAttributes) {
        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMsg", "Mật khẩu xác nhận không khớp.");
            return "redirect:/reset-password?token=" + token;
        }
        if (password.length() < 6) {
            redirectAttributes.addFlashAttribute("errorMsg", "Mật khẩu phải có ít nhất 6 ký tự.");
            return "redirect:/reset-password?token=" + token;
        }
        try {
            passwordResetService.resetPassword(token, password);
            redirectAttributes.addFlashAttribute("successMsg", "Đặt lại mật khẩu thành công! Hãy đăng nhập.");
            return "redirect:/login";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/forgot-password";
        }
    }
}
