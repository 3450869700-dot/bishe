package com.peachshop.controller;

import com.peachshop.model.Coupon;
import com.peachshop.model.UserCoupon;
import com.peachshop.service.CouponService;
import com.peachshop.service.UserCouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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
        
        try {
            // 获取参数
            Long userId = data.containsKey("userId") ? Long.parseLong(data.get("userId").toString()) : 1L;
            Long couponId = data.containsKey("couponId") ? Long.parseLong(data.get("couponId").toString()) : null;
            Integer limitPerUser = data.containsKey("limitPerUser") ? Integer.parseInt(data.get("limitPerUser").toString()) : 3;
            
            System.out.println("=== fetchCoupon: userId=" + userId + ", couponId=" + couponId + ", limitPerUser=" + limitPerUser);
            
            if (couponId == null) {
                response.put("code", 400);
                response.put("msg", "优惠券ID不能为空");
                response.put("data", null);
                return ResponseEntity.ok(response);
            }
            
            // 查询用户已领取该优惠券的数量
            long receivedCount = userCouponService.countByUserIdAndCouponId(userId, couponId);
            System.out.println("=== fetchCoupon: user " + userId + " has received " + receivedCount + " coupons of id " + couponId);
            
            // 检查是否超过领取上限
            if (receivedCount >= limitPerUser) {
                response.put("code", 403);
                response.put("msg", "您已达到领取上限，每人限领" + limitPerUser + "张");
                response.put("data", null);
                return ResponseEntity.ok(response);
            }
            
            // 创建新的用户优惠券记录
            UserCoupon userCoupon = new UserCoupon();
            userCoupon.setUserId(userId);
            userCoupon.setCouponId(couponId);
            userCoupon.setCouponCode("COUPON_" + System.currentTimeMillis()); // 生成优惠券码
            userCoupon.setUseStatus(1); // 1表示未使用
            userCoupon.setReceiveTime(LocalDateTime.now());
            
            // 保存到数据库
            UserCoupon savedCoupon = userCouponService.save(userCoupon);
            System.out.println("=== fetchCoupon: saved coupon " + savedCoupon.getUserCouponId() + " for user " + userId);
            
            response.put("code", 0);
            response.put("msg", "领取成功");
            response.put("data", savedCoupon);
            
        } catch (Exception e) {
            System.out.println("=== fetchCoupon: error " + e.getMessage());
            e.printStackTrace();
            response.put("code", 500);
            response.put("msg", "领取失败：" + e.getMessage());
            response.put("data", null);
        }
        
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
        
        // 从请求参数获取用户ID，默认值为1
        Long userId = 1L;
        if (params.containsKey("userId")) {
            try {
                userId = Long.parseLong(params.get("userId").toString());
            } catch (Exception e) {
                // 解析失败时使用默认值
                System.out.println("=== getMyCoupons: Invalid userId, using default: " + userId);
            }
        }
        System.out.println("=== getMyCoupons: Getting coupons for userId: " + userId);
        
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
        
        System.out.println("=== getMyCoupons: Found " + userCoupons.size() + " coupons for userId: " + userId);
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
