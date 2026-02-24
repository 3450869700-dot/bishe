package com.peachshop.service;

import com.peachshop.model.UserCoupon;
import com.peachshop.repository.UserCouponRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserCouponService {
    @Autowired
    private UserCouponRepository userCouponRepository;
    
    public Optional<UserCoupon> findById(Long id) {
        return userCouponRepository.findById(id);
    }
    
    public List<UserCoupon> findByUserId(Long userId) {
        return userCouponRepository.findByUserId(userId);
    }
    
    public List<UserCoupon> findByUserIdAndUseStatus(Long userId, Integer useStatus) {
        return userCouponRepository.findByUserIdAndUseStatus(userId, useStatus);
    }
    
    public UserCoupon findByCouponCode(String couponCode) {
        return userCouponRepository.findByCouponCode(couponCode);
    }
    
    public UserCoupon save(UserCoupon userCoupon) {
        return userCouponRepository.save(userCoupon);
    }
    
    public void deleteById(Long id) {
        userCouponRepository.deleteById(id);
    }
}