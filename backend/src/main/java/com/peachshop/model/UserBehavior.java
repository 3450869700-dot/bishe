package com.peachshop.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 用户行为数据实体
 * 用于记录用户与商品的交互行为，支持推荐系统的协同过滤算法
 */
@Entity
@Table(name = "user_behaviors")
public class UserBehavior {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "behavior_type", nullable = false, length = 20)
    private String behaviorType; // VIEW, FAVORITE, CART, PURCHASE, SEARCH

    @Column(name = "behavior_weight")
    private Double behaviorWeight; // 行为权重

    @Column(name = "stay_duration") // 停留时长（秒），用于浏览行为
    private Integer stayDuration;

    @Column(name = "search_keyword", length = 100) // 搜索关键词
    private String searchKeyword;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // 构造函数
    public UserBehavior() {
        this.createdAt = LocalDateTime.now();
    }

    public UserBehavior(Integer userId, Long productId, String behaviorType) {
        this.userId = userId;
        this.productId = productId;
        this.behaviorType = behaviorType;
        this.behaviorWeight = getDefaultWeight(behaviorType);
        this.createdAt = LocalDateTime.now();
    }

    // 获取默认行为权重
    private Double getDefaultWeight(String type) {
        return switch (type.toUpperCase()) {
            case "VIEW" -> 1.0;
            case "SEARCH" -> 2.0;
            case "FAVORITE" -> 3.0;
            case "CART" -> 4.0;
            case "PURCHASE" -> 5.0;
            default -> 1.0;
        };
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getBehaviorType() {
        return behaviorType;
    }

    public void setBehaviorType(String behaviorType) {
        this.behaviorType = behaviorType;
        this.behaviorWeight = getDefaultWeight(behaviorType);
    }

    public Double getBehaviorWeight() {
        return behaviorWeight;
    }

    public void setBehaviorWeight(Double behaviorWeight) {
        this.behaviorWeight = behaviorWeight;
    }

    public Integer getStayDuration() {
        return stayDuration;
    }

    public void setStayDuration(Integer stayDuration) {
        this.stayDuration = stayDuration;
    }

    public String getSearchKeyword() {
        return searchKeyword;
    }

    public void setSearchKeyword(String searchKeyword) {
        this.searchKeyword = searchKeyword;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "UserBehavior{" +
                "id=" + id +
                ", userId=" + userId +
                ", productId=" + productId +
                ", behaviorType='" + behaviorType + '\'' +
                ", behaviorWeight=" + behaviorWeight +
                ", createdAt=" + createdAt +
                '}';
    }
}
