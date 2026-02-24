package com.peachshop.service;

import com.peachshop.model.Coupon;
import com.peachshop.repository.CouponRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CouponService {
    @Autowired
    private CouponRepository couponRepository;
    
    public Optional<Coupon> findById(Long id) {
        return couponRepository.findById(id);
    }
    
    public List<Coupon> findAll() {
        return couponRepository.findAll();
    }
    
    public List<Coupon> findByIsValid(Integer isValid) {
        return couponRepository.findByIsValid(isValid);
    }
    
    public List<Coupon> findByProductScope(Integer productScope) {
        return couponRepository.findByProductScope(productScope);
    }
    
    public Coupon save(Coupon coupon) {
        return couponRepository.save(coupon);
    }
    
    public void deleteById(Long id) {
        couponRepository.deleteById(id);
    }
}