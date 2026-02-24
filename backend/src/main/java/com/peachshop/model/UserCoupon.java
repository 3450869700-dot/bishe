package com.peachshop.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_coupon")
public class UserCoupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_coupon_id")
    private Long userCouponId;
    
    @Column(name = "user_id")
    private Long userId;
    
    @Column(name = "coupon_id")
    private Long couponId;
    
    @Column(name = "coupon_code")
    private String couponCode;
    
    @Column(name = "use_status")
    private Integer useStatus;
    
    @Column(name = "receive_time")
    private LocalDateTime receiveTime;
    
    @Column(name = "use_time")
    private LocalDateTime useTime;
    
    @Column(name = "order_id")
    private Long orderId;
    
    // Getters and Setters
    public Long getUserCouponId() {
        return userCouponId;
    }
    
    public void setUserCouponId(Long userCouponId) {
        this.userCouponId = userCouponId;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public Long getCouponId() {
        return couponId;
    }
    
    public void setCouponId(Long couponId) {
        this.couponId = couponId;
    }
    
    public String getCouponCode() {
        return couponCode;
    }
    
    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }
    
    public Integer getUseStatus() {
        return useStatus;
    }
    
    public void setUseStatus(Integer useStatus) {
        this.useStatus = useStatus;
    }
    
    public LocalDateTime getReceiveTime() {
        return receiveTime;
    }
    
    public void setReceiveTime(LocalDateTime receiveTime) {
        this.receiveTime = receiveTime;
    }
    
    public LocalDateTime getUseTime() {
        return useTime;
    }
    
    public void setUseTime(LocalDateTime useTime) {
        this.useTime = useTime;
    }
    
    public Long getOrderId() {
        return orderId;
    }
    
    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
}