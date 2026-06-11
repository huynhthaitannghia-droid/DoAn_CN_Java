package com.huit.CN_Java.service;

import com.huit.CN_Java.entity.Coupon;
import com.huit.CN_Java.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;

    public List<Coupon> findAll() {
        return couponRepository.findAll();
    }

    public Coupon findByIdOrThrow(Long id) {
        return couponRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy mã giảm giá ID: " + id));
    }

    public void save(Coupon coupon) {
        couponRepository.save(coupon);
    }

    public void deleteById(Long id) {
        couponRepository.deleteById(id);
    }

    public void toggleActive(Long id) {
        Coupon coupon = findByIdOrThrow(id);
        coupon.setActive(!coupon.isActive());
        couponRepository.save(coupon);
    }
}