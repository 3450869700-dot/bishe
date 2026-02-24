package com.peachshop.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupon")
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coupon_id")
    private Long couponId;
    
    @Column(name = "coupon_name")
    private String couponName;
    
    @Column(name = "coupon_type")
    private Integer couponType;
    
    @Column(name = "face_value")
    private Double faceValue;
    
    @Column(name = "min_amount")
    private Double minAmount;
    
    @Column(name = "product_scope")
    private Integer productScope;
    
    @Column(name = "start_time")
    private LocalDateTime startTime;
    
    @Column(name = "end_time")
    private LocalDateTime endTime;
    
    @Column(name = "total_quantity")
    private Integer totalQuantity;
    
    @Column(name = "remain_quantity")
    private Integer remainQuantity;
    
    @Column(name = "user_limit")
    private Integer userLimit;
    
    @Column(name = "is_valid")
    private Integer isValid;
    
    @Column(name = "create_time")
    private LocalDateTime createTime;
    
    @Column(name = "update_time")
    private LocalDateTime updateTime;
    
    // Getters and Setters
    public Long getCouponId() {
        return couponId;
    }
    
    public void setCouponId(Long couponId) {
        this.couponId = couponId;
    }
    
    public String getCouponName() {
        return couponName;
    }
    
    public void setCouponName(String couponName) {
        this.couponName = couponName;
    }
    
    public Integer getCouponType() {
        return couponType;
    }
    
    public void setCouponType(Integer couponType) {
        this.couponType = couponType;
    }
    
    public Double getFaceValue() {
        return faceValue;
    }
    
    public void setFaceValue(Double faceValue) {
        this.faceValue = faceValue;
    }
    
    public Double getMinAmount() {
        return minAmount;
    }
    
    public void setMinAmount(Double minAmount) {
        this.minAmount = minAmount;
    }
    
    public Integer getProductScope() {
        return productScope;
    }
    
    public void setProductScope(Integer productScope) {
        this.productScope = productScope;
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    
    public Integer getTotalQuantity() {
        return totalQuantity;
    }
    
    public void setTotalQuantity(Integer totalQuantity) {
        this.totalQuantity = totalQuantity;
    }
    
    public Integer getRemainQuantity() {
        return remainQuantity;
    }
    
    public void setRemainQuantity(Integer remainQuantity) {
        this.remainQuantity = remainQuantity;
    }
    
    public Integer getUserLimit() {
        return userLimit;
    }
    
    public void setUserLimit(Integer userLimit) {
        this.userLimit = userLimit;
    }
    
    public Integer getIsValid() {
        return isValid;
    }
    
    public void setIsValid(Integer isValid) {
        this.isValid = isValid;
    }
    
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
    
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }
    
    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}