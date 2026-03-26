package com.peachshop.repository;

import com.peachshop.model.UserBehavior;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserBehaviorRepository extends JpaRepository<UserBehavior, Long> {

    /**
     * 获取用户的所有行为记录
     */
    List<UserBehavior> findByUserId(Integer userId);

    /**
     * 获取用户对特定商品的行为记录
     */
    List<UserBehavior> findByUserIdAndProductId(Integer userId, Long productId);

    /**
     * 获取特定类型的行为记录
     */
    List<UserBehavior> findByUserIdAndBehaviorType(Integer userId, String behaviorType);

    /**
     * 获取商品的所有行为记录
     */
    List<UserBehavior> findByProductId(Long productId);

    /**
     * 获取时间范围内的行为记录
     */
    @Query("SELECT ub FROM UserBehavior ub WHERE ub.userId = :userId AND ub.createdAt >= :startTime")
    List<UserBehavior> findRecentBehaviors(@Param("userId") Integer userId, @Param("startTime") LocalDateTime startTime);

    /**
     * 获取用户行为统计（用于协同过滤）
     */
    @Query("SELECT ub.productId, SUM(ub.behaviorWeight) as totalWeight " +
           "FROM UserBehavior ub WHERE ub.userId = :userId " +
           "GROUP BY ub.productId ORDER BY totalWeight DESC")
    List<Object[]> getUserProductPreferences(@Param("userId") Integer userId);

    /**
     * 获取相似用户（基于共同行为）
     */
    @Query("SELECT ub2.userId, COUNT(DISTINCT ub1.productId) as commonProducts " +
           "FROM UserBehavior ub1 " +
           "JOIN UserBehavior ub2 ON ub1.productId = ub2.productId " +
           "WHERE ub1.userId = :userId AND ub2.userId != :userId " +
           "GROUP BY ub2.userId " +
           "HAVING COUNT(DISTINCT ub1.productId) >= 2 " +
           "ORDER BY commonProducts DESC")
    List<Object[]> findSimilarUsers(@Param("userId") Integer userId);

    /**
     * 获取用户搜索关键词
     */
    @Query("SELECT DISTINCT ub.searchKeyword FROM UserBehavior ub " +
           "WHERE ub.userId = :userId AND ub.behaviorType = 'SEARCH' " +
           "AND ub.searchKeyword IS NOT NULL")
    List<String> findUserSearchKeywords(@Param("userId") Integer userId);

    /**
     * 删除过期数据（数据清理）
     */
    void deleteByCreatedAtBefore(LocalDateTime date);
}
