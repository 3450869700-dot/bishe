package com.peachshop.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class RecommendationService {
    private final ProductService productService;

    @Autowired
    public RecommendationService(ProductService productService) {
        this.productService = productService;
    }

    /**
     * 从价格字符串中提取数字
     * 例如："33元/箱" -> 33.0, "1.8元/斤" -> 1.8
     */
    private double extractPrice(String priceStr) {
        if (priceStr == null || priceStr.isEmpty()) {
            return 0.0;
        }
        
        // 使用正则表达式提取价格数字
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

    public Mono<List<Map<String, Object>>> getRecommendations(int userId) {
        try {
            // 从实际商品数据库中获取商品
            List<com.peachshop.model.Product> products = productService.getProducts();
            
            // 随机选择10个商品作为推荐
            List<Map<String, Object>> recommendations = new ArrayList<>();
            
            // 确保不重复选择商品
            List<com.peachshop.model.Product> shuffledProducts = new ArrayList<>(products);
            java.util.Collections.shuffle(shuffledProducts);
            
            // 选择前10个商品
            int count = 0;
            for (com.peachshop.model.Product product : shuffledProducts) {
                if (count >= 10) break;
                
                try {
                    Map<String, Object> recommendation = new HashMap<>();
                    recommendation.put("id", product.getProductCode());
                    recommendation.put("name", product.getName());
                    recommendation.put("category", "general");
                    
                    // 提取价格数字
                    double priceValue = extractPrice(product.getPrice());
                    recommendation.put("price", priceValue);
                    
                    // 从数据库获取评分，如果没有则使用默认值
                    Double rating = product.getRating();
                    recommendation.put("rating", rating != null ? rating : 4.0);
                    
                    // 从数据库获取库存，如果没有则使用默认值
                    Integer stock = product.getStock();
                    recommendation.put("stock", stock != null ? stock : 100);
                    
                    // 添加图片URL
                    recommendation.put("pic", product.getImageUrl());
                    
                    // 添加图片列表（与商品详情页格式一致）
                    List<Map<String, String>> pics = new ArrayList<>();
                    if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                        Map<String, String> pic = new HashMap<>();
                        pic.put("id", "1");
                        pic.put("pic", product.getImageUrl());
                        pics.add(pic);
                    }
                    recommendation.put("pics", pics);
                    
                    // 添加热度 - heat是String类型，需要转换为Integer
                    String heatStr = product.getHeat();
                    int heatValue = 0;
                    if (heatStr != null && !heatStr.isEmpty()) {
                        try {
                            heatValue = Integer.parseInt(heatStr);
                        } catch (NumberFormatException e) {
                            heatValue = 0;
                        }
                    }
                    recommendation.put("heat", heatValue);
                    
                    // 添加规格信息
                    recommendation.put("specifications", getProductSpecifications(product));
                    
                    recommendations.add(recommendation);
                    count++;
                } catch (Exception e) {
                    // 忽略单个商品的错误，继续处理其他商品
                    continue;
                }
            }
            
            return Mono.just(recommendations);
        } catch (Exception e) {
            // 返回空列表作为默认值
            return Mono.just(new ArrayList<>());
        }
    }

    public Mono<List<Map<String, Object>>> getPopularItems() {
        try {
            // 从实际商品数据库中获取商品
            List<com.peachshop.model.Product> products = productService.getProducts();
            
            // 随机选择10个商品作为热门商品
            List<Map<String, Object>> popularItems = new ArrayList<>();
            
            // 确保不重复选择商品
            List<com.peachshop.model.Product> shuffledProducts = new ArrayList<>(products);
            java.util.Collections.shuffle(shuffledProducts);
            
            // 选择前10个商品
            int count = 0;
            for (com.peachshop.model.Product product : shuffledProducts) {
                if (count >= 10) break;
                
                try {
                    Map<String, Object> popularItem = new HashMap<>();
                    popularItem.put("id", product.getProductCode());
                    popularItem.put("name", product.getName());
                    popularItem.put("category", "general");
                    
                    // 提取价格数字
                    double priceValue = extractPrice(product.getPrice());
                    popularItem.put("price", priceValue);
                    
                    // 从数据库获取评分，如果没有则使用默认值
                    Double rating = product.getRating();
                    popularItem.put("rating", rating != null ? rating : 4.0);
                    
                    // 从数据库获取库存，如果没有则使用默认值
                    Integer stock = product.getStock();
                    popularItem.put("stock", stock != null ? stock : 100);
                    
                    // 添加图片URL
                    popularItem.put("pic", product.getImageUrl());
                    
                    // 添加图片列表（与商品详情页格式一致）
                    List<Map<String, String>> pics = new ArrayList<>();
                    if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                        Map<String, String> pic = new HashMap<>();
                        pic.put("id", "1");
                        pic.put("pic", product.getImageUrl());
                        pics.add(pic);
                    }
                    popularItem.put("pics", pics);
                    
                    // 添加热度 - heat是String类型，需要转换为Integer
                    String heatStr = product.getHeat();
                    int heatValue = 0;
                    if (heatStr != null && !heatStr.isEmpty()) {
                        try {
                            heatValue = Integer.parseInt(heatStr);
                        } catch (NumberFormatException e) {
                            heatValue = 0;
                        }
                    }
                    popularItem.put("heat", heatValue);
                    
                    // 添加规格信息
                    popularItem.put("specifications", getProductSpecifications(product));
                    
                    popularItems.add(popularItem);
                    count++;
                } catch (Exception e) {
                    // 忽略单个商品的错误，继续处理其他商品
                    continue;
                }
            }
            
            return Mono.just(popularItems);
        } catch (Exception e) {
            // 返回空列表作为默认值
            return Mono.just(new ArrayList<>());
        }
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
            // 如果出错，返回默认规格
            Map<String, Object> defaultSpec = new HashMap<>();
            defaultSpec.put("id", product.getProductCode());
            defaultSpec.put("specification", "默认规格");
            defaultSpec.put("price", 0.0);
            defaultSpec.put("stock", 100);
            specs.add(defaultSpec);
        }
        
        return specs;
    }
}
