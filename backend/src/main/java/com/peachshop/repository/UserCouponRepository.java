package com.peachshop.repository;

import com.peachshop.model.UserCoupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserCouponRepository extends JpaRepository<UserCoupon, Long> {
    List<UserCoupon> findByUserId(Long userId);
    List<UserCoupon> findByUserIdAndUseStatus(Long userId, Integer useStatus);
    UserCoupon findByCouponCode(String couponCode);
    
    /**
     * 查询用户已领取某张优惠券的数量
     */
    long countByUserIdAndCouponId(Long userId, Long couponId);
}