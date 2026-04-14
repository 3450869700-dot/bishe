package com.peachshop.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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
 * 
 * 更新说明：热度推荐改用逻辑回归计算（已注释）
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
        
        // 未来版本：启用混合推荐（使用逻辑回归计算热度）
        // return getHybridRecommendations(userId);
    }
    
    /**
     * 热门商品接口 - 当前使用随机推荐
     * 未来版本：返回基于热度的推荐
     */
    public Mono<List<Map<String, Object>>> getPopularItems() {
        // 当前版本：使用随机推荐
        return getRandomRecommendations();
        
        // 未来版本：启用热度推荐（使用逻辑回归）
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
    
    // ==================== 混合推荐算法框架（未来启用，含逻辑回归热度计算） ====================
    
    /*
     * 以下代码已注释，未来启用混合推荐时取消注释
     * 主要更新：热度推荐使用逻辑回归替代简单归一化
     */
    
    /*
    // ==================== 逻辑回归热度推荐配置 ====================
    
    // 逻辑回归模型参数（热度推荐）
    private Map<String, Double> lrWeights = new HashMap<>();
    private double lrBias = 0.0;
    
    // 特征统计（用于标准化）
    private Map<String, Double> featureMeans = new HashMap<>();
    private Map<String, Double> featureStdDevs = new HashMap<>();
    
    // 训练参数
    private static final double LR_LEARNING_RATE = 0.01;
    private static final int LR_MAX_ITERATIONS = 500;
    private static final double LR_CONVERGENCE_THRESHOLD = 1e-6;
    
    // ==================== 混合推荐主入口 ====================
    
    private Mono<List<Map<String, Object>>> getHybridRecommendations(int userId) {
        try {
            // 获取所有商品
            List<com.peachshop.model.Product> allProducts = productService.getProducts();
            
            // 1. 热度推荐结果（使用逻辑回归计算）
            List<ScoredProduct> popularityScores = calculatePopularityScoresWithLR(allProducts);
            
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
    
    // ==================== 算法1：基于逻辑回归的热度推荐 ====================
    
    private List<ScoredProduct> calculatePopularityScoresWithLR(List<com.peachshop.model.Product> products) {
        // 初始化模型（如果未训练）
        if (lrWeights.isEmpty()) {
            trainLogisticRegressionModel(products);
        }
        
        List<ScoredProduct> scores = new ArrayList<>();
        
        for (com.peachshop.model.Product product : products) {
            // 提取特征
            HeatFeatures features = extractHeatFeatures(product);
            
            // 逻辑回归预测热度得分
            double heatScore = predictHeatScoreWithLR(features);
            
            scores.add(new ScoredProduct(product, heatScore, "popularity_lr"));
        }
        
        return scores;
    }
    
    private void trainLogisticRegressionModel(List<com.peachshop.model.Product> products) {
        // 准备训练数据
        List<HeatTrainingData> trainingData = new ArrayList<>();
        
        for (com.peachshop.model.Product product : products) {
            HeatFeatures features = extractHeatFeatures(product);
            double label = isHotProduct(features) ? 1.0 : 0.0;
            trainingData.add(new HeatTrainingData(features, label));
        }
        
        if (trainingData.size() < 50) {
            // 数据不足，使用默认权重
            initializeDefaultLRWeights();
            return;
        }
        
        // 计算特征统计
        calculateFeatureStatistics(trainingData);
        
        // 初始化权重
        initializeLRWeights();
        
        // 梯度下降训练
        lrGradientDescent(trainingData);
    }
    
    private HeatFeatures extractHeatFeatures(com.peachshop.model.Product product) {
        HeatFeatures features = new HeatFeatures();
        
        // 从heat字段解析热度值
        try {
            features.heatValue = Integer.parseInt(product.getHeat());
        } catch (Exception e) {
            features.heatValue = 0;
        }
        
        // 时间特征（上架天数）
        features.daysSincePublished = ChronoUnit.DAYS.between(
            product.getCreatedAt() != null ? product.getCreatedAt() : LocalDateTime.now().minusDays(30),
            LocalDateTime.now()
        );
        
        // 质量特征
        features.rating = product.getRating() != null ? product.getRating() : 4.0;
        
        // 价格特征（提取数字）
        features.priceValue = extractPrice(product.getPrice());
        
        // 对数特征（处理长尾）
        features.logHeatValue = Math.log(features.heatValue + 1);
        
        // 时间衰减特征
        features.timeDecay = Math.exp(-0.01 * features.daysSincePublished);
        
        return features;
    }
    
    private boolean isHotProduct(HeatFeatures features) {
        // 定义热门商品标准
        return features.heatValue > 500 && features.rating > 4.0;
    }
    
    private double predictHeatScoreWithLR(HeatFeatures features) {
        double z = lrBias;
        
        z += lrWeights.getOrDefault("heatValue", 0.0) * normalize(features.heatValue, "heatValue");
        z += lrWeights.getOrDefault("daysSincePublished", 0.0) * normalize(features.daysSincePublished, "daysSincePublished");
        z += lrWeights.getOrDefault("rating", 0.0) * normalize(features.rating, "rating");
        z += lrWeights.getOrDefault("priceValue", 0.0) * normalize(features.priceValue, "priceValue");
        z += lrWeights.getOrDefault("logHeatValue", 0.0) * normalize(features.logHeatValue, "logHeatValue");
        z += lrWeights.getOrDefault("timeDecay", 0.0) * normalize(features.timeDecay, "timeDecay");
        
        return sigmoid(z);
    }
    
    private void lrGradientDescent(List<HeatTrainingData> data) {
        int n = data.size();
        double prevLoss = Double.MAX_VALUE;
        
        String[] featureNames = {"heatValue", "daysSincePublished", "rating", "priceValue", "logHeatValue", "timeDecay"};
        
        for (int iter = 0; iter < LR_MAX_ITERATIONS; iter++) {
            Map<String, Double> gradients = new HashMap<>();
            double biasGradient = 0.0;
            double totalLoss = 0.0;
            
            for (HeatTrainingData sample : data) {
                double prediction = predictRaw(sample.features);
                double error = prediction - sample.label;
                totalLoss += calculateLRLoss(sample.label, sigmoid(prediction));
                
                // 计算各特征梯度
                for (String feature : featureNames) {
                    double value = getFeatureValue(sample.features, feature);
                    double normalizedValue = normalize(value, feature);
                    gradients.merge(feature, error * normalizedValue, Double::sum);
                }
                biasGradient += error;
            }
            
            // 更新权重
            for (String feature : featureNames) {
                if (gradients.containsKey(feature)) {
                    double gradient = gradients.get(feature) / n;
                    double newWeight = lrWeights.get(feature) - LR_LEARNING_RATE * gradient;
                    lrWeights.put(feature, newWeight);
                }
            }
            lrBias -= LR_LEARNING_RATE * (biasGradient / n);
            
            // 检查收敛
            double avgLoss = totalLoss / n;
            if (Math.abs(prevLoss - avgLoss) < LR_CONVERGENCE_THRESHOLD) {
                break;
            }
            prevLoss = avgLoss;
        }
    }
    
    private double predictRaw(HeatFeatures features) {
        double z = lrBias;
        String[] featureNames = {"heatValue", "daysSincePublished", "rating", "priceValue", "logHeatValue", "timeDecay"};
        
        for (String feature : featureNames) {
            double value = getFeatureValue(features, feature);
            double weight = lrWeights.getOrDefault(feature, 0.0);
            z += weight * normalize(value, feature);
        }
        
        return z;
    }
    
    private double getFeatureValue(HeatFeatures features, String featureName) {
        return switch (featureName) {
            case "heatValue" -> features.heatValue;
            case "daysSincePublished" -> features.daysSincePublished;
            case "rating" -> features.rating;
            case "priceValue" -> features.priceValue;
            case "logHeatValue" -> features.logHeatValue;
            case "timeDecay" -> features.timeDecay;
            default -> 0.0;
        };
    }
    
    private void calculateFeatureStatistics(List<HeatTrainingData> data) {
        String[] featureNames = {"heatValue", "daysSincePublished", "rating", "priceValue", "logHeatValue", "timeDecay"};
        
        for (String feature : featureNames) {
            List<Double> values = data.stream()
                .map(d -> getFeatureValue(d.features, feature))
                .collect(Collectors.toList());
            
            double mean = values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            double variance = values.stream()
                .mapToDouble(v -> Math.pow(v - mean, 2))
                .average().orElse(0.0);
            double stdDev = Math.sqrt(variance);
            
            featureMeans.put(feature, mean);
            featureStdDevs.put(feature, stdDev > 0 ? stdDev : 1.0);
        }
    }
    
    private void initializeLRWeights() {
        lrWeights.clear();
        String[] featureNames = {"heatValue", "daysSincePublished", "rating", "priceValue", "logHeatValue", "timeDecay"};
        for (String feature : featureNames) {
            lrWeights.put(feature, 0.0);
        }
        lrBias = 0.0;
    }
    
    private void initializeDefaultLRWeights() {
        // 默认权重（基于经验）
        lrWeights.put("heatValue", 2.0);
        lrWeights.put("daysSincePublished", -0.5);
        lrWeights.put("rating", 1.0);
        lrWeights.put("priceValue", 0.2);
        lrWeights.put("logHeatValue", 0.5);
        lrWeights.put("timeDecay", 1.5);
        lrBias = -1.0;
    }
    
    private double normalize(double value, String feature) {
        double mean = featureMeans.getOrDefault(feature, 0.0);
        double stdDev = featureStdDevs.getOrDefault(feature, 1.0);
        return (value - mean) / stdDev;
    }
    
    private double sigmoid(double z) {
        return 1.0 / (1.0 + Math.exp(-z));
    }
    
    private double calculateLRLoss(double label, double prediction) {
        prediction = Math.max(1e-15, Math.min(1 - 1e-15, prediction));
        return -(label * Math.log(prediction) + (1 - label) * Math.log(1 - prediction));
    }
    
    // ==================== 热度特征类 ====================
    
    private static class HeatFeatures {
        double heatValue;           // 热度值
        double daysSincePublished;  // 上架天数
        double rating;              // 评分
        double priceValue;          // 价格
        double logHeatValue;        // 热度对数
        double timeDecay;           // 时间衰减因子
    }
    
    private static class HeatTrainingData {
        HeatFeatures features;
        double label;
        
        HeatTrainingData(HeatFeatures features, double label) {
            this.features = features;
            this.label = label;
        }
    }
    
    // ==================== 算法2：协同过滤（框架） ====================
    
    private List<ScoredProduct> calculateCollaborativeScores(
            int userId, 
            List<com.peachshop.model.Product> products) {
        
        List<ScoredProduct> scores = new ArrayList<>();
        
        // TODO: 需要用户行为数据支持
        // 1. 获取当前用户的历史行为
        // 2. 计算用户相似度（余弦相似度）
        // 3. 找到K个最相似的用户
        // 4. 推荐这些用户喜欢的商品
        
        for (com.peachshop.model.Product product : products) {
            scores.add(new ScoredProduct(product, 0.5, "collaborative"));
        }
        
        return scores;
    }
    
    // ==================== 算法3：内容推荐（框架） ====================
    
    private List<ScoredProduct> calculateContentScores(
            int userId, 
            List<com.peachshop.model.Product> products) {
        
        List<ScoredProduct> scores = new ArrayList<>();
        
        // TODO: 需要用户历史偏好数据
        // 1. 获取用户历史喜欢的商品
        // 2. 提取商品特征（品种、等级、价格、产地）
        // 3. 计算候选商品与用户偏好的相似度
        
        for (com.peachshop.model.Product product : products) {
            scores.add(new ScoredProduct(product, 0.5, "content"));
        }
        
        return scores;
    }
    
    // ==================== 加权融合 ====================
    
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
    */
    
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
}
