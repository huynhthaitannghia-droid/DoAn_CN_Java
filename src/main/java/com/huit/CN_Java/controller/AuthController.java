package com.huit.CN_Java.controller;

import com.huit.CN_Java.dto.RegisterDTO;
import com.huit.CN_Java.service.UserService;
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
}