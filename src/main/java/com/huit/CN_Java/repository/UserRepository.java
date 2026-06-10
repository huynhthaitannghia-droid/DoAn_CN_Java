package com.huit.CN_Java.repository;

import com.huit.CN_Java.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE :keyword='' OR LOWER(u.fullName) LIKE LOWER(CONCAT('%',:keyword,'%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%',:keyword,'%')) OR u.phone LIKE %:keyword%")
    Page<User> searchAdmin(@Param("keyword") String keyword, Pageable pageable);
}
