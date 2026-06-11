package com.huit.CN_Java.service;

import com.huit.CN_Java.dto.RegisterDTO;
import com.huit.CN_Java.entity.User;
import com.huit.CN_Java.entity.Role;
import com.huit.CN_Java.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public void register(RegisterDTO dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email đã được sử dụng");
        }

        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setFullName(dto.getFullName());
        user.setPhone(dto.getPhone());
        user.setRole(Role.USER);
        user.setEnabled(true);

        userRepository.save(user);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));
    }

    // --- CÁC HÀM BỔ SUNG CHO ADMIN ---
    public long countAllUsers() {
        return userRepository.count();
    }

    public org.springframework.data.domain.Page<com.huit.CN_Java.entity.User> searchAdmin(String keyword, org.springframework.data.domain.Pageable pageable) {
        return userRepository.searchAdmin(keyword != null ? keyword : "", pageable);
    }

    public void toggleLock(Long id) {
        com.huit.CN_Java.entity.User user = findByIdOrThrow(id);
        if (user.getRole() == com.huit.CN_Java.entity.Role.ADMIN && !user.isLocked()) {
            throw new RuntimeException("Không thể khóa tài khoản Admin");
        }
        user.setLocked(!user.isLocked());
        userRepository.save(user);
    }

    public void setRole(Long id, com.huit.CN_Java.entity.Role role) {
        com.huit.CN_Java.entity.User user = findByIdOrThrow(id);
        user.setRole(role);
        userRepository.save(user);
    }

    public com.huit.CN_Java.entity.User findByIdOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
    }
}