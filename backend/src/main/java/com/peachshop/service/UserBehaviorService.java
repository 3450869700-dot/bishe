package com.peachshop.service;

import com.peachshop.model.UserBehavior;
import com.peachshop.repository.UserBehaviorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 用户行为数据服务
 * 负责收集、存储和分析用户行为数据
 */
@Service
public class UserBehaviorService {

    @Autowired
    private UserBehaviorRepository userBehaviorRepository;

    // ==================== 行为记录方法 ====================

    /**
     * 记录用户浏览行为
     * 在商品详情页加载时调用
     */
    public void recordView(Integer userId, Long productId, Integer stayDuration) {
        UserBehavior behavior = new UserBehavior(userId, productId, "VIEW");
        behavior.setStayDuration(stayDuration);
        userBehaviorRepository.save(behavior);
    }

    /**
     * 记录用户收藏行为
     * 在用户点击收藏按钮时调用
     */
    public void recordFavorite(Integer userId, Long productId) {
        // 检查是否已存在收藏记录
        List<UserBehavior> existing = userBehaviorRepository
            .findByUserIdAndProductId(userId, productId)
            .stream()
            .filter(b -> "FAVORITE".equals(b.getBehaviorType()))
            .toList();
        
        if (existing.isEmpty()) {
            UserBehavior behavior = new UserBehavior(userId, productId, "FAVORITE");
            userBehaviorRepository.save(behavior);
        }
    }

    /**
     * 记录用户加购行为
     * 在加入购物车时调用
     */
    public void recordCart(Integer userId, Long productId) {
        UserBehavior behavior = new UserBehavior(userId, productId, "CART");
        userBehaviorRepository.save(behavior);
    }

    /**
     * 记录用户购买行为
     * 在完成订单时调用
     */
    public void recordPurchase(Integer userId, Long productId) {
        UserBehavior behavior = new UserBehavior(userId, productId, "PURCHASE");
        userBehaviorRepository.save(behavior);
    }

    /**
     * 记录用户搜索行为
     * 在执行搜索时调用
     */
    public void recordSearch(Integer userId, String keyword) {
        UserBehavior behavior = new UserBehavior();
        behavior.setUserId(userId);
        behavior.setBehaviorType("SEARCH");
        behavior.setSearchKeyword(keyword);
        behavior.setBehaviorWeight(2.0);
        behavior.setCreatedAt(LocalDateTime.now());
        userBehaviorRepository.save(behavior);
    }

    // ==================== 数据分析方法（用于推荐算法）====================

    /**
     * 获取用户偏好商品（用于协同过滤）
     */
    public Map<Long, Double> getUserPreferences(Integer userId) {
        Map<Long, Double> preferences = new HashMap<>();
        List<Object[]> results = userBehaviorRepository.getUserProductPreferences(userId);
        
        for (Object[] result : results) {
            Long productId = (Long) result[0];
            Double weight = ((Number) result[1]).doubleValue();
            preferences.put(productId, weight);
        }
        
        return preferences;
    }

    /**
     * 获取相似用户列表（用于协同过滤）
     */
    public List<Integer> getSimilarUsers(Integer userId, int limit) {
        List<Object[]> results = userBehaviorRepository.findSimilarUsers(userId);
        List<Integer> similarUsers = new ArrayList<>();
        
        int count = 0;
        for (Object[] result : results) {
            if (count >= limit) break;
            Integer similarUserId = (Integer) result[0];
            Long commonProducts = ((Number) result[1]).longValue();
            similarUsers.add(similarUserId);
            count++;
        }
        
        return similarUsers;
    }

    /**
     * 获取用户搜索关键词（用于内容推荐）
     */
    public List<String> getUserSearchKeywords(Integer userId) {
        return userBehaviorRepository.findUserSearchKeywords(userId);
    }

    /**
     * 获取用户最近行为（用于实时推荐）
     */
    public List<UserBehavior> getRecentBehaviors(Integer userId, int days) {
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);
        return userBehaviorRepository.findRecentBehaviors(userId, startTime);
    }

    /**
     * 获取用户行为统计
     */
    public Map<String, Object> getUserBehaviorStats(Integer userId) {
        Map<String, Object> stats = new HashMap<>();
        List<UserBehavior> behaviors = userBehaviorRepository.findByUserId(userId);
        
        // 统计各类型行为数量
        Map<String, Long> typeCount = new HashMap<>();
        for (UserBehavior behavior : behaviors) {
            String type = behavior.getBehaviorType();
            typeCount.put(type, typeCount.getOrDefault(type, 0L) + 1);
        }
        
        stats.put("totalBehaviors", behaviors.size());
        stats.put("typeDistribution", typeCount);
        stats.put("uniqueProducts", behaviors.stream()
            .map(UserBehavior::getProductId)
            .distinct()
            .count());
        
        return stats;
    }

    // ==================== 数据清理方法 ====================

    /**
     * 清理过期数据（保留最近90天的数据）
     */
    public void cleanupOldData() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(90);
        userBehaviorRepository.deleteByCreatedAtBefore(cutoffDate);
    }
}
