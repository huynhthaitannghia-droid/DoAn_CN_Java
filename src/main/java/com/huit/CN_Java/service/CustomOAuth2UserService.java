package com.huit.CN_Java.service;

import com.huit.CN_Java.entity.Role;
import com.huit.CN_Java.entity.User;
import com.huit.CN_Java.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String email  = oAuth2User.getAttribute("email");
        String name   = oAuth2User.getAttribute("name");

        // Tìm hoặc tạo user trong DB
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setFullName(name != null ? name : email);
            newUser.setPassword(UUID.randomUUID().toString());
            newUser.setRole(Role.USER);
            newUser.setEnabled(true);
            newUser.setOauth2Provider(userRequest.getClientRegistration().getRegistrationId()); // "google"
            return userRepository.save(newUser);
        });
        // Nếu đã có tài khoản thường → gắn provider nếu chưa có
        if (user.getOauth2Provider() == null) {
            user.setOauth2Provider(userRequest.getClientRegistration().getRegistrationId());
            userRepository.save(user);
        }

        // Trả về CustomUserDetails thay vì DefaultOAuth2User
        return new com.huit.CN_Java.security.CustomUserDetails(user, oAuth2User.getAttributes());
    }
}