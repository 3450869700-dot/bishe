package com.peachshop.controller;

import com.peachshop.model.Coupon;
import com.peachshop.model.UserCoupon;
import com.peachshop.service.CouponService;
import com.peachshop.service.UserCouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/discounts")
public class DiscountController {

    @Autowired
    private CouponService couponService;
    
    @Autowired
    private UserCouponService userCouponService;

    /**
     * 获取可领取优惠券列表
     */
    @GetMapping("/coupons")
    public ResponseEntity<Map<String, Object>> getCoupons(@RequestParam Map<String, Object> params) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("msg", "success");
        // 返回有效的优惠券列表
        List<Coupon> coupons = couponService.findByIsValid(1);
        response.put("data", coupons);
        return ResponseEntity.ok(response);
    }

    /**
     * 优惠券规格详情
     */
    @GetMapping("/detail")
    public ResponseEntity<Map<String, Object>> getDiscountDetail(@RequestParam Map<String, Object> params) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("msg", "success");
        
        // 根据id获取优惠券详情
        if (params.containsKey("id")) {
            try {
                Long id = Long.parseLong(params.get("id").toString());
                Coupon coupon = couponService.findById(id).orElse(null);
                response.put("data", coupon);
            } catch (Exception e) {
                response.put("data", new HashMap<>());
            }
        } else {
            response.put("data", new HashMap<>());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 使用优惠券-红包的动态口令兑换优惠券
     */
    @PostMapping("/exchange")
    public ResponseEntity<Map<String, Object>> exchangeCoupon(@RequestBody Map<String, Object> data) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("msg", "success");
        response.put("data", new HashMap<>()); // 返回空对象
        return ResponseEntity.ok(response);
    }

    /**
     * 领取优惠券
     */
    @PostMapping("/fetch")
    public ResponseEntity<Map<String, Object>> fetchCoupon(@RequestBody Map<String, Object> data) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("msg", "success");
        response.put("data", new HashMap<>()); // 返回空对象
        return ResponseEntity.ok(response);
    }

    /**
     * 我的优惠券
     */
    @GetMapping("/my")
    public ResponseEntity<Map<String, Object>> getMyCoupons(@RequestParam Map<String, Object> params) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("msg", "success");
        
        // 模拟用户ID为1，实际项目中应该从登录状态获取
        Long userId = 1L;
        
        // 根据状态获取优惠券
        List<UserCoupon> userCoupons = new ArrayList<>();
        if (params.containsKey("status")) {
            try {
                Integer status = Integer.parseInt(params.get("status").toString());
                userCoupons = userCouponService.findByUserIdAndUseStatus(userId, status);
            } catch (Exception e) {
                userCoupons = userCouponService.findByUserId(userId);
            }
        } else {
            userCoupons = userCouponService.findByUserId(userId);
        }
        
        response.put("data", userCoupons);
        return ResponseEntity.ok(response);
    }

    /**
     * 优惠券统计
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getDiscountStatistics(@RequestParam Map<String, Object> params) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("msg", "success");
        response.put("data", new HashMap<>()); // 返回空对象
        return ResponseEntity.ok(response);
    }
}
