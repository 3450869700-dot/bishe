package com.peachshop.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 混合推荐算法服务
 * 
 * 结合多种推荐算法的加权混合推荐系统：
 * 1. 热度推荐 (Popularity-based) - 权重: 0.4
 * 2. 协同过滤 (Collaborative Filtering) - 权重: 0.35
 * 3. 内容推荐 (Content-based) - 权重: 0.25
 * 
 * 当前版本：仅实现算法框架，实际仍使用随机推荐
 * 未来版本：启用加权计算，融合多种推荐结果
 */
@Service
public class HybridRecommendationService {
    
    private final ProductService productService;
    
    // 算法权重配置（可配置化）
    private static final double WEIGHT_POPULARITY = 0.40;    // 热度推荐权重
    private static final double WEIGHT_COLLABORATIVE = 0.35; // 协同过滤权重
    private static final double WEIGHT_CONTENT = 0.25;       // 内容推荐权重
    
    // 推荐数量配置
    private static final int RECOMMENDATION_COUNT = 10;
    
    @Autowired
    public HybridRecommendationService(ProductService productService) {
        this.productService = productService;
    }
    
    /**
     * 主推荐接口 - 当前使用随机推荐
     * 未来版本：返回加权混合推荐结果
     */
    public Mono<List<Map<String, Object>>> getRecommendations(int userId) {
        // 当前版本：使用随机推荐
        return getRandomRecommendations();
        
        // 未来版本：启用混合推荐
        // return getHybridRecommendations(userId);
    }
    
    /**
     * 热门商品接口 - 当前使用随机推荐
     * 未来版本：返回基于热度的推荐
     */
    public Mono<List<Map<String, Object>>> getPopularItems() {
        // 当前版本：使用随机推荐
        return getRandomRecommendations();
        
        // 未来版本：启用热度推荐
        // return getPopularityBasedRecommendations();
    }
    
    // ==================== 当前使用的随机推荐算法 ====================
    
    /**
     * 随机推荐算法（当前实际使用的算法）
     */
    private Mono<List<Map<String, Object>>> getRandomRecommendations() {
        try {
            List<com.peachshop.model.Product> products = productService.getProducts();
            List<Map<String, Object>> recommendations = new ArrayList<>();
            
            List<com.peachshop.model.Product> shuffledProducts = new ArrayList<>(products);
            Collections.shuffle(shuffledProducts);
            
            int count = 0;
            for (com.peachshop.model.Product product : shuffledProducts) {
                if (count >= RECOMMENDATION_COUNT) break;
                
                try {
                    Map<String, Object> recommendation = buildProductMap(product);
                    recommendations.add(recommendation);
                    count++;
                } catch (Exception e) {
                    continue;
                }
            }
            
            return Mono.just(recommendations);
        } catch (Exception e) {
            return Mono.just(new ArrayList<>());
        }
    }
    
    // ==================== 混合推荐算法框架（未来启用） ====================
    
    /**
     * 混合推荐算法（框架已实现，未来启用）
     * 
     * 算法流程：
     * 1. 分别获取三种算法的推荐结果
     * 2. 计算每个商品的加权得分
     * 3. 按得分排序，返回Top N
     */
    private Mono<List<Map<String, Object>>> getHybridRecommendations(int userId) {
        try {
            // 获取所有商品
            List<com.peachshop.model.Product> allProducts = productService.getProducts();
            
            // 1. 热度推荐结果
            List<ScoredProduct> popularityScores = calculatePopularityScores(allProducts);
            
            // 2. 协同过滤结果（需要用户行为数据）
            List<ScoredProduct> collaborativeScores = calculateCollaborativeScores(userId, allProducts);
            
            // 3. 内容推荐结果（基于用户历史偏好）
            List<ScoredProduct> contentScores = calculateContentScores(userId, allProducts);
            
            // 4. 加权融合
            List<ScoredProduct> hybridScores = mergeScores(
                popularityScores, 
                collaborativeScores, 
                contentScores
            );
            
            // 5. 排序并返回Top N
            List<Map<String, Object>> recommendations = hybridScores.stream()
                .sorted((a, b) -> Double.compare(b.score, a.score))
                .limit(RECOMMENDATION_COUNT)
                .map(sp -> buildProductMap(sp.product))
                .collect(Collectors.toList());
            
            return Mono.just(recommendations);
            
        } catch (Exception e) {
            // 出错时回退到随机推荐
            return getRandomRecommendations();
        }
    }
    
    /**
     * 算法1：热度推荐（Popularity-based）
     * 
     * 基于商品的heat字段（热度值）计算得分
     * 热度越高，得分越高
     */
    private List<ScoredProduct> calculatePopularityScores(List<com.peachshop.model.Product> products) {
        List<ScoredProduct> scores = new ArrayList<>();
        
        // 找出最大热度值用于归一化
        int maxHeat = products.stream()
            .mapToInt(p -> {
                try {
                    return Integer.parseInt(p.getHeat());
                } catch (Exception e) {
                    return 0;
                }
            })
            .max()
            .orElse(1);
        
        for (com.peachshop.model.Product product : products) {
            try {
                int heat = Integer.parseInt(product.getHeat());
                // 归一化得分：heat / maxHeat
                double score = maxHeat > 0 ? (double) heat / maxHeat : 0;
                scores.add(new ScoredProduct(product, score, "popularity"));
            } catch (Exception e) {
                scores.add(new ScoredProduct(product, 0, "popularity"));
            }
        }
        
        return scores;
    }
    
    /**
     * 算法2：协同过滤（Collaborative Filtering）
     * 
     * 基于用户行为数据的协同过滤推荐
     * 需要：用户-商品交互数据（浏览、购买、收藏等）
     * 
     * 实现思路：
     * 1. 找到与当前用户相似的其他用户
     * 2. 推荐相似用户喜欢的商品
     */
    private List<ScoredProduct> calculateCollaborativeScores(
            int userId, 
            List<com.peachshop.model.Product> products) {
        
        List<ScoredProduct> scores = new ArrayList<>();
        
        // TODO: 需要用户行为数据支持
        // 1. 获取当前用户的历史行为（浏览、购买、收藏）
        // 2. 计算用户相似度（余弦相似度、皮尔逊相关系数等）
        // 3. 找到K个最相似的用户
        // 4. 推荐这些用户喜欢但当前用户未交互的商品
        
        // 当前：返回默认得分（需要数据支持后才能实现）
        for (com.peachshop.model.Product product : products) {
            scores.add(new ScoredProduct(product, 0.5, "collaborative"));
        }
        
        return scores;
    }
    
    /**
     * 算法3：内容推荐（Content-based）
     * 
     * 基于商品属性特征的推荐
     * 分析用户历史喜欢的商品特征，推荐相似特征的商品
     * 
     * 考虑的特征：
     * - 商品品种（variety）
     * - 商品等级（grade）
     * - 价格区间
     * - 产地（address）
     */
    private List<ScoredProduct> calculateContentScores(
            int userId, 
            List<com.peachshop.model.Product> products) {
        
        List<ScoredProduct> scores = new ArrayList<>();
        
        // TODO: 需要用户历史偏好数据
        // 1. 获取用户历史喜欢的商品
        // 2. 提取这些商品的特征（品种、等级、价格区间、产地）
        // 3. 计算每个候选商品与用户偏好的相似度
        // 4. 返回相似度得分
        
        // 当前：返回默认得分（需要数据支持后才能实现）
        for (com.peachshop.model.Product product : products) {
            scores.add(new ScoredProduct(product, 0.5, "content"));
        }
        
        return scores;
    }
    
    /**
     * 加权融合三种算法的得分
     * 
     * 公式：final_score = w1 * popularity + w2 * collaborative + w3 * content
     */
    private List<ScoredProduct> mergeScores(
            List<ScoredProduct> popularityScores,
            List<ScoredProduct> collaborativeScores,
            List<ScoredProduct> contentScores) {
        
        Map<Long, ScoredProduct> mergedMap = new HashMap<>();
        
        // 合并热度推荐得分
        for (ScoredProduct sp : popularityScores) {
            Long productId = sp.product.getProductCode();
            double weightedScore = sp.score * WEIGHT_POPULARITY;
            mergedMap.put(productId, new ScoredProduct(sp.product, weightedScore, "hybrid"));
        }
        
        // 合并协同过滤得分
        for (ScoredProduct sp : collaborativeScores) {
            Long productId = sp.product.getProductCode();
            double weightedScore = sp.score * WEIGHT_COLLABORATIVE;
            mergedMap.merge(productId, 
                new ScoredProduct(sp.product, weightedScore, "hybrid"),
                (existing, newScore) -> {
                    existing.score += newScore.score;
                    return existing;
                });
        }
        
        // 合并内容推荐得分
        for (ScoredProduct sp : contentScores) {
            Long productId = sp.product.getProductCode();
            double weightedScore = sp.score * WEIGHT_CONTENT;
            mergedMap.merge(productId, 
                new ScoredProduct(sp.product, weightedScore, "hybrid"),
                (existing, newScore) -> {
                    existing.score += newScore.score;
                    return existing;
                });
        }
        
        return new ArrayList<>(mergedMap.values());
    }
    
    // ==================== 工具方法 ====================
    
    /**
     * 构建商品推荐数据Map
     */
    private Map<String, Object> buildProductMap(com.peachshop.model.Product product) {
        Map<String, Object> recommendation = new HashMap<>();
        recommendation.put("id", product.getProductCode());
        recommendation.put("name", product.getName());
        recommendation.put("category", "general");
        recommendation.put("price", extractPrice(product.getPrice()));
        recommendation.put("rating", product.getRating() != null ? product.getRating() : 4.0);
        recommendation.put("stock", product.getStock() != null ? product.getStock() : 100);
        recommendation.put("pic", product.getImageUrl());
        
        // 图片列表
        List<Map<String, String>> pics = new ArrayList<>();
        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            Map<String, String> pic = new HashMap<>();
            pic.put("id", "1");
            pic.put("pic", product.getImageUrl());
            pics.add(pic);
        }
        recommendation.put("pics", pics);
        
        // 热度
        int heatValue = 0;
        try {
            heatValue = Integer.parseInt(product.getHeat());
        } catch (Exception e) {
            heatValue = 0;
        }
        recommendation.put("heat", heatValue);
        
        // 规格信息
        recommendation.put("specifications", getProductSpecifications(product));
        
        return recommendation;
    }
    
    /**
     * 从价格字符串中提取数字
     */
    private double extractPrice(String priceStr) {
        if (priceStr == null || priceStr.isEmpty()) {
            return 0.0;
        }
        Pattern pattern = Pattern.compile("(\\d+\\.\\d+|\\d+)");
        Matcher matcher = pattern.matcher(priceStr);
        if (matcher.find()) {
            try {
                return Double.parseDouble(matcher.group(1));
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
        return 0.0;
    }
    
    /**
     * 获取商品规格信息
     */
    private List<Map<String, Object>> getProductSpecifications(com.peachshop.model.Product product) {
        List<Map<String, Object>> specs = new ArrayList<>();
        try {
            Map<String, Object> spec = new HashMap<>();
            spec.put("id", product.getProductCode());
            spec.put("specification", product.getSpecName() != null && !product.getSpecName().isEmpty() 
                    ? product.getSpecName() : "默认规格");
            spec.put("price", extractPrice(product.getPrice()));
            spec.put("stock", product.getStock() != null ? product.getStock() : 100);
            spec.put("pic", product.getImageUrl());
            specs.add(spec);
        } catch (Exception e) {
            Map<String, Object> defaultSpec = new HashMap<>();
            defaultSpec.put("id", product.getProductCode());
            defaultSpec.put("specification", "默认规格");
            defaultSpec.put("price", 0.0);
            defaultSpec.put("stock", 100);
            specs.add(defaultSpec);
        }
        return specs;
    }
    
    /**
     * 带得分的商品包装类
     */
    private static class ScoredProduct {
        com.peachshop.model.Product product;
        double score;
        String algorithmType;
        
        ScoredProduct(com.peachshop.model.Product product, double score, String algorithmType) {
            this.product = product;
            this.score = score;
            this.algorithmType = algorithmType;
        }
    }
    
    // ==================== 数据模型（未来需要实现） ====================
    
    /**
     * 用户行为数据模型
     * 用于协同过滤算法
     */
    public static class UserBehavior {
        int userId;
        Long productId;
        String behaviorType; // VIEW, PURCHASE, FAVORITE, CART
        double weight;       // 行为权重
        long timestamp;
    }
    
    /**
     * 用户偏好特征模型
     * 用于内容推荐算法
     */
    public static class UserPreference {
        int userId;
        Map<String, Double> varietyPreferences;  // 品种偏好
        Map<String, Double> gradePreferences;    // 等级偏好
        double preferredPriceMin;                // 偏好价格区间
        double preferredPriceMax;
        Map<String, Double> addressPreferences;  // 产地偏好
    }
}
