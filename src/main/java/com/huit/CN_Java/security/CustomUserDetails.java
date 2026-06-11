package com.huit.CN_Java.security;

import com.huit.CN_Java.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class CustomUserDetails implements UserDetails, OAuth2User {

    @Getter
    private final User user;
    private Map<String, Object> attributes; // chỉ dùng khi login OAuth2

    // Constructor cho đăng nhập thường
    public CustomUserDetails(User user) {
        this.user = user;
    }

    // Constructor cho OAuth2
    public CustomUserDetails(User user, Map<String, Object> attributes) {
        this.user = user;
        this.attributes = attributes;
    }

    // ===== UserDetails =====
    @Override public String getUsername() { return user.getEmail(); }
    @Override public String getPassword() { return user.getPassword(); }
    @Override public boolean isEnabled()  { return user.isEnabled(); }
    @Override public boolean isAccountNonLocked()    { return !user.isLocked(); }
    @Override public boolean isAccountNonExpired()   { return true; }
    @Override public boolean isCredentialsNonExpired(){ return true; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    // ===== OAuth2User =====
    @Override public Map<String, Object> getAttributes() { return attributes; }
    @Override public String getName() { return user.getEmail(); }
}