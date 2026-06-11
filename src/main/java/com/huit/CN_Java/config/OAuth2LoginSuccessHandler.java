package com.huit.CN_Java.config;

import com.huit.CN_Java.entity.Role;
import com.huit.CN_Java.entity.User;
import com.huit.CN_Java.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String name  = oAuth2User.getAttribute("name");

        // Tự động tạo tài khoản nếu chưa có
        if (email != null && !userRepository.existsByEmail(email)) {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setFullName(name != null ? name : email);
            newUser.setPassword(UUID.randomUUID().toString()); // password random, không dùng
            newUser.setRole(Role.USER);
            newUser.setEnabled(true);
            userRepository.save(newUser);
        }

        getRedirectStrategy().sendRedirect(request, response, "/");
    }
}