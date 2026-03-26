package com.peachshop.repository;

import com.peachshop.model.UserCoupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserCouponRepository extends JpaRepository<UserCoupon, Long> {
    List<UserCoupon> findByUserId(Long userId);
    List<UserCoupon> findByUserIdAndUseStatus(Long userId, Integer useStatus);
    UserCoupon findByCouponCode(String couponCode);
    
    /**
     * 查询用户已领取某张优惠券的数量
     */
    long countByUserIdAndCouponId(Long userId, Long couponId);
    
    /**
     * 查询所有用户的优惠券领取数量
     */
    @Query("SELECT userId, COUNT(*) FROM UserCoupon GROUP BY userId")
    List<Object[]> findCountByUserIdGroupByUserId();
    
    /**
     * 查询每个优惠券的总领取数量
     */
    @Query("SELECT couponId, COUNT(*) FROM UserCoupon GROUP BY couponId")
    List<Object[]> findCountByCouponIdGroupByCouponId();
}