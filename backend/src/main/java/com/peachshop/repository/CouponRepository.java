package com.peachshop.repository;

import com.peachshop.model.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    List<Coupon> findByIsValid(Integer isValid);
    List<Coupon> findByProductScope(Integer productScope);
}