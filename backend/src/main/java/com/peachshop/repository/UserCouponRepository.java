package com.peachshop.repository;

import com.peachshop.model.UserCoupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserCouponRepository extends JpaRepository<UserCoupon, Long> {
    List<UserCoupon> findByUserId(Long userId);
    List<UserCoupon> findByUserIdAndUseStatus(Long userId, Integer useStatus);
    UserCoupon findByCouponCode(String couponCode);
}