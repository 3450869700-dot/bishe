/*
 * ============================================
 * 点击率预测服务 - 基于逻辑回归算法
 * ============================================
 * 注意：此代码已注释掉，不会影响当前程序运行
 * 如需启用，请取消注释并在 ProductService 中调用相关方法
 * ============================================
 */

// package com.peachshop.service;

// import com.peachshop.model.Product;
// import com.peachshop.model.UserBehavior;
// import com.peachshop.repository.ProductRepository;
// import com.peachshop.repository.UserBehaviorRepository;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.scheduling.annotation.Scheduled;
// import org.springframework.stereotype.Service;

// import jakarta.annotation.PostConstruct;
// import java.time.LocalDateTime;
// import java.time.temporal.ChronoUnit;
// import java.util.*;
// import java.util.concurrent.ConcurrentHashMap;
// import java.util.stream.Collectors;

/**
 * 点击率预测服务 - 基于逻辑回归算法
 * 
 * 功能说明：
 * 1. 使用逻辑回归模型预测每个商品的点击率
 * 2. 基于 user_behaviors 表中的用户行为数据训练模型
 * 3. 提供实时预测接口，返回点击率最高的5个商品
 * 4. 支持定时重新训练模型
 * 
 * 特征工程：
 * - 商品浏览次数
 * - 商品收藏次数
 * - 商品加购次数
 * - 商品购买次数
 * - 平均停留时长
 * - 转化率（购买/浏览）
 * - 收藏率（收藏/浏览）
 * - 加购率（加购/浏览）
 * - 商品上架时间
 * - 商品类别热度
 */
// @Service
// public class ClickThroughRatePredictionService {

    // @Autowired
    // private UserBehaviorRepository userBehaviorRepository;

    // @Autowired
    // private ProductRepository productRepository;

    // 模型参数（逻辑回归权重）
    // private Map<String, Double> modelWeights = new ConcurrentHashMap<>();
    
    // 缓存预测结果
    // private Map<Long, Double> ctrPredictions = new ConcurrentHashMap<>();
    
    // 特征均值和标准差（用于标准化）
    // private Map<String, Double> featureMeans = new ConcurrentHashMap<>();
    // private Map<String, Double> featureStdDevs = new ConcurrentHashMap<>();

    // 学习率和迭代次数
    // private static final double LEARNING_RATE = 0.01;
    // private static final int MAX_ITERATIONS = 1000;
    // private static final double CONVERGENCE_THRESHOLD = 1e-6;

    /**
     * 初始化：加载模型并训练
     */
    // @PostConstruct
    // public void init() {
    //     System.out.println("=== CTR Prediction Service: Initializing ===");
    //     trainModel();
    //     System.out.println("=== CTR Prediction Service: Initialized ===");
    // }

    /**
     * 定时重新训练模型（每天凌晨2点）
     */
    // @Scheduled(cron = "0 0 2 * * ?")
    // public void scheduledTraining() {
    //     System.out.println("=== CTR Prediction Service: Scheduled Training Started ===");
    //     trainModel();
    //     System.out.println("=== CTR Prediction Service: Scheduled Training Completed ===");
    // }

    /**
     * 训练逻辑回归模型
     */
    // public void trainModel() {
    //     try {
    //         // 1. 获取训练数据
    //         List<TrainingData> trainingData = prepareTrainingData();
    //         
    //         if (trainingData.isEmpty()) {
    //             System.out.println("=== CTR Prediction Service: No training data available ===");
    //             return;
    //         }

    //         System.out.println("=== CTR Prediction Service: Training with " + trainingData.size() + " samples ===");

    //         // 2. 计算特征统计（用于标准化）
    //         calculateFeatureStatistics(trainingData);

    //         // 3. 初始化模型权重
    //         initializeWeights();

    //         // 4. 梯度下降训练
    //         gradientDescent(trainingData);

    //         // 5. 更新预测缓存
    //         updatePredictions();

    //         System.out.println("=== CTR Prediction Service: Training Completed ===");
    //         System.out.println("=== Model Weights: " + modelWeights + " ===");

    //     } catch (Exception e) {
    //         System.err.println("=== CTR Prediction Service: Training Failed ===");
    //         e.printStackTrace();
    //     }
    // }

    /**
     * 准备训练数据
     */
    // private List<TrainingData> prepareTrainingData() {
    //     List<TrainingData> data = new ArrayList<>();
    //     
    //     // 获取最近30天的行为数据
    //     LocalDateTime startTime = LocalDateTime.now().minus(30, ChronoUnit.DAYS);
    //     List<UserBehavior> behaviors = userBehaviorRepository.findAll().stream()
    //         .filter(b -> b.getCreatedAt() != null && b.getCreatedAt().isAfter(startTime))
    //         .collect(Collectors.toList());

    //     // 按商品分组统计
    //     Map<Long, ProductStats> statsMap = new HashMap<>();
    //     
    //     for (UserBehavior behavior : behaviors) {
    //         Long productId = behavior.getProductId();
    //         ProductStats stats = statsMap.computeIfAbsent(productId, k -> new ProductStats());
    //         
    //         switch (behavior.getBehaviorType().toUpperCase()) {
    //             case "VIEW":
    //                 stats.viewCount++;
    //                 if (behavior.getStayDuration() != null) {
    //                     stats.totalStayDuration += behavior.getStayDuration();
    //                 }
    //                 break;
    //             case "FAVORITE":
    //                 stats.favoriteCount++;
    //                 break;
    //             case "CART":
    //                 stats.cartCount++;
    //                 break;
    //             case "PURCHASE":
    //                 stats.purchaseCount++;
    //                 break;
    //         }
    //     }

    //     // 构建训练样本
    //     for (Map.Entry<Long, ProductStats> entry : statsMap.entrySet()) {
    //         Long productId = entry.getKey();
    //         ProductStats stats = entry.getValue();
    //         
    //         // 计算特征
    //         Map<String, Double> features = calculateFeatures(stats);
    //         
    //         // 计算标签（点击率）：有购买或加购行为的视为正样本
    //         double label = (stats.purchaseCount > 0 || stats.cartCount > 0) ? 1.0 : 0.0;
    //         
    //         // 只有浏览次数大于5的商品才加入训练
    //         if (stats.viewCount >= 5) {
    //             data.add(new TrainingData(productId, features, label));
    //         }
    //     }

    //     return data;
    // }

    /**
     * 计算特征
     */
    // private Map<String, Double> calculateFeatures(ProductStats stats) {
    //     Map<String, Double> features = new HashMap<>();
    //     
    //     // 基础特征
    //     features.put("viewCount", (double) stats.viewCount);
    //     features.put("favoriteCount", (double) stats.favoriteCount);
    //     features.put("cartCount", (double) stats.cartCount);
    //     features.put("purchaseCount", (double) stats.purchaseCount);
    //     features.put("avgStayDuration", stats.viewCount > 0 ? 
    //         (double) stats.totalStayDuration / stats.viewCount : 0.0);
    //     
    //     // 比率特征
    //     features.put("conversionRate", stats.viewCount > 0 ? 
    //         (double) stats.purchaseCount / stats.viewCount : 0.0);
    //     features.put("favoriteRate", stats.viewCount > 0 ? 
    //         (double) stats.favoriteCount / stats.viewCount : 0.0);
    //     features.put("cartRate", stats.viewCount > 0 ? 
    //         (double) stats.cartCount / stats.viewCount : 0.0);
    //     
    //     // 对数特征（处理长尾分布）
    //     features.put("logViewCount", Math.log(stats.viewCount + 1));
    //     features.put("logFavoriteCount", Math.log(stats.favoriteCount + 1));
    //     features.put("logCartCount", Math.log(stats.cartCount + 1));
    //     features.put("logPurchaseCount", Math.log(stats.purchaseCount + 1));
    //     
    //     // 交互特征
    //     features.put("engagementScore", 
    //         stats.favoriteCount * 2.0 + stats.cartCount * 3.0 + stats.purchaseCount * 5.0);
    //     
    //     return features;
    // }

    /**
     * 计算特征统计（用于标准化）
     */
    // private void calculateFeatureStatistics(List<TrainingData> data) {
    //     if (data.isEmpty()) return;
    //     
    //     Set<String> featureNames = data.get(0).features.keySet();
    //     
    //     for (String feature : featureNames) {
    //         List<Double> values = data.stream()
    //             .map(d -> d.features.getOrDefault(feature, 0.0))
    //             .collect(Collectors.toList());
    //         
    //         double mean = values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    //         double variance = values.stream()
    //             .mapToDouble(v -> Math.pow(v - mean, 2))
    //             .average().orElse(0.0);
    //         double stdDev = Math.sqrt(variance);
    //         
    //         featureMeans.put(feature, mean);
    //         featureStdDevs.put(feature, stdDev > 0 ? stdDev : 1.0); // 避免除以0
    //     }
    // }

    /**
     * 初始化模型权重
     */
    // private void initializeWeights() {
    //     modelWeights.clear();
    //     // 初始化权重为0或小的随机值
    //     String[] features = {"viewCount", "favoriteCount", "cartCount", "purchaseCount", 
    //         "avgStayDuration", "conversionRate", "favoriteRate", "cartRate",
    //         "logViewCount", "logFavoriteCount", "logCartCount", "logPurchaseCount",
    //         "engagementScore"};
    //     
    //     for (String feature : features) {
    //         modelWeights.put(feature, 0.0);
    //     }
    //     modelWeights.put("bias", 0.0); // 偏置项
    // }

    /**
     * 梯度下降训练
     */
    // private void gradientDescent(List<TrainingData> data) {
    //     int n = data.size();
    //     double prevLoss = Double.MAX_VALUE;
    //     
    //     for (int iter = 0; iter < MAX_ITERATIONS; iter++) {
    //         Map<String, Double> gradients = new HashMap<>();
    //         double totalLoss = 0.0;
    //         
    //         // 计算梯度
    //         for (TrainingData sample : data) {
    //             double prediction = predictRaw(sample.features);
    //             double error = prediction - sample.label;
    //             totalLoss += calculateLoss(sample.label, sigmoid(prediction));
    //             
    //             // 计算每个特征的梯度
    //             for (String feature : sample.features.keySet()) {
    //                 double normalizedValue = normalize(sample.features.get(feature), feature);
    //                 gradients.merge(feature, error * normalizedValue, Double::sum);
    //             }
    //             // 偏置项梯度
    //             gradients.merge("bias", error, Double::sum);
    //         }
    //         
    //         // 更新权重
    //         for (String feature : modelWeights.keySet()) {
    //             if (gradients.containsKey(feature)) {
    //                 double gradient = gradients.get(feature) / n;
    //                 double newWeight = modelWeights.get(feature) - LEARNING_RATE * gradient;
    //                 modelWeights.put(feature, newWeight);
    //             }
    //         }
    //         
    //         // 检查收敛
    //         double avgLoss = totalLoss / n;
    //         if (Math.abs(prevLoss - avgLoss) < CONVERGENCE_THRESHOLD) {
    //             System.out.println("=== CTR Prediction Service: Converged at iteration " + iter + " ===");
    //             break;
    //         }
    //         prevLoss = avgLoss;
    //         
    //         // 每100次迭代打印一次损失
    //         if (iter % 100 == 0) {
    //             System.out.println("=== Iteration " + iter + ", Loss: " + avgLoss + " ===");
    //         }
    //     }
    // }

    /**
     * 预测（原始值，未经过sigmoid）
     */
    // private double predictRaw(Map<String, Double> features) {
    //     double result = modelWeights.getOrDefault("bias", 0.0);
    //     
    //     for (Map.Entry<String, Double> entry : features.entrySet()) {
    //         String feature = entry.getKey();
    //         double value = entry.getValue();
    //         double weight = modelWeights.getOrDefault(feature, 0.0);
    //         result += weight * normalize(value, feature);
    //     }
    //     
    //     return result;
    // }

    /**
     * 预测点击率
     */
    // public double predictCTR(Long productId) {
    //     // 如果缓存中有，直接返回
    //     if (ctrPredictions.containsKey(productId)) {
    //         return ctrPredictions.get(productId);
    //     }
    //     
    //     // 实时计算
    //     List<UserBehavior> behaviors = userBehaviorRepository.findByProductId(productId);
    //     if (behaviors.isEmpty()) {
    //         return 0.5; // 默认点击率
    //     }
    //     
    //     ProductStats stats = new ProductStats();
    //     for (UserBehavior behavior : behaviors) {
    //         switch (behavior.getBehaviorType().toUpperCase()) {
    //             case "VIEW":
    //                 stats.viewCount++;
    //                 if (behavior.getStayDuration() != null) {
    //                     stats.totalStayDuration += behavior.getStayDuration();
    //                 }
    //                 break;
    //             case "FAVORITE":
    //                 stats.favoriteCount++;
    //                 break;
    //             case "CART":
    //                 stats.cartCount++;
    //                 break;
    //             case "PURCHASE":
    //                 stats.purchaseCount++;
    //                 break;
    //         }
    //     }
    //     
    //     Map<String, Double> features = calculateFeatures(stats);
    //     double rawPrediction = predictRaw(features);
    //     double ctr = sigmoid(rawPrediction);
    //     
    //     return ctr;
    // }

    /**
     * 获取点击率最高的5个商品
     */
    // public List<Product> getTop5ProductsByCTR() {
    //     List<Product> allProducts = productRepository.findAll();
    //     
    //     return allProducts.stream()
    //         .map(p -> new AbstractMap.SimpleEntry<>(p, predictCTR(p.getId())))
    //         .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
    //         .limit(5)
    //         .map(Map.Entry::getKey)
    //         .collect(Collectors.toList());
    // }

    /**
     * 获取点击率最高的N个商品（带预测值）
     */
    // public List<ProductWithCTR> getTopNProductsByCTR(int n) {
    //     List<Product> allProducts = productRepository.findAll();
    //     
    //     return allProducts.stream()
    //         .map(p -> new ProductWithCTR(p, predictCTR(p.getId())))
    //         .sorted((e1, e2) -> Double.compare(e2.getCtr(), e1.getCtr()))
    //         .limit(n)
    //         .collect(Collectors.toList());
    // }

    /**
     * 更新预测缓存
     */
    // private void updatePredictions() {
    //     ctrPredictions.clear();
    //     List<Product> products = productRepository.findAll();
    //     
    //     for (Product product : products) {
    //         double ctr = predictCTR(product.getId());
    //         ctrPredictions.put(product.getId(), ctr);
    //     }
    //     
    //     System.out.println("=== CTR Prediction Service: Updated " + ctrPredictions.size() + " predictions ===");
    // }

    /**
     * Sigmoid函数
     */
    // private double sigmoid(double z) {
    //     return 1.0 / (1.0 + Math.exp(-z));
    // }

    /**
     * 标准化特征
     */
    // private double normalize(double value, String feature) {
    //     double mean = featureMeans.getOrDefault(feature, 0.0);
    //     double stdDev = featureStdDevs.getOrDefault(feature, 1.0);
    //     return (value - mean) / stdDev;
    // }

    /**
     * 计算对数损失
     */
    // private double calculateLoss(double label, double prediction) {
    //     // 避免log(0)
    //     prediction = Math.max(1e-15, Math.min(1 - 1e-15, prediction));
    //     return -(label * Math.log(prediction) + (1 - label) * Math.log(1 - prediction));
    // }

    /**
     * 获取模型权重（用于调试）
     */
    // public Map<String, Double> getModelWeights() {
    //     return new HashMap<>(modelWeights);
    // }

    /**
     * 获取特征统计（用于调试）
     */
    // public Map<String, Double> getFeatureStatistics() {
    //     Map<String, Double> stats = new HashMap<>();
    //     stats.putAll(featureMeans);
    //     return stats;
    // }

    // ==================== 内部类 ====================

    /**
     * 训练数据
     */
    // private static class TrainingData {
    //     Long productId;
    //     Map<String, Double> features;
    //     double label;

    //     TrainingData(Long productId, Map<String, Double> features, double label) {
    //         this.productId = productId;
    //         this.features = features;
    //         this.label = label;
    //     }
    // }

    /**
     * 商品统计
     */
    // private static class ProductStats {
    //     int viewCount = 0;
    //     int favoriteCount = 0;
    //     int cartCount = 0;
    //     int purchaseCount = 0;
    //     int totalStayDuration = 0;
    // }

    /**
     * 带点击率的商品
     */
    // public static class ProductWithCTR {
    //     private Product product;
    //     private double ctr;

    //     public ProductWithCTR(Product product, double ctr) {
    //         this.product = product;
    //         this.ctr = ctr;
    //     }

    //     public Product getProduct() {
    //         return product;
    //     }

    //     public double getCtr() {
    //         return ctr;
    //     }
    // }
// }

/*
 * ============================================
 * 使用说明：
 * ============================================
 * 
 * 1. 在 ProductService 中注入此服务：
 *    @Autowired
 *    private ClickThroughRatePredictionService ctrPredictionService;
 * 
 * 2. 获取点击率最高的5个商品：
 *    List<Product> top5Products = ctrPredictionService.getTop5ProductsByCTR();
 * 
 * 3. 获取带点击率值的商品列表：
 *    List<ProductWithCTR> productsWithCTR = ctrPredictionService.getTopNProductsByCTR(5);
 * 
 * 4. 预测单个商品的点击率：
 *    double ctr = ctrPredictionService.predictCTR(productId);
 * 
 * 5. 手动触发模型训练：
 *    ctrPredictionService.trainModel();
 * 
 * ============================================
 * 注意事项：
 * ============================================
 * 
 * 1. 此服务需要足够的行为数据才能准确预测
 *    建议至少有1000条以上的用户行为记录
 * 
 * 2. 模型每天凌晨2点自动重新训练
 *    可以通过 @Scheduled 注解修改训练频率
 * 
 * 3. 特征工程可以根据业务需求调整
 *    当前特征包括：浏览、收藏、加购、购买等行为
 * 
 * 4. 预测结果会缓存，提高查询性能
 *    缓存会在模型重新训练时更新
 * 
 * 5. 新商品（没有行为数据）会返回默认点击率0.5
 *    可以通过冷启动策略优化
 * 
 * ============================================
 */
