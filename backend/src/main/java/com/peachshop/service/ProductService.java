package com.peachshop.service;

import com.peachshop.model.Product;
import com.peachshop.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;
    
    // 缓存机制
    private static final ConcurrentHashMap<String, Object> CACHE = new ConcurrentHashMap<>();
    private static final long CACHE_EXPIRE_TIME = 300000; // 缓存过期时间：5分钟
    private static final String ALL_PRODUCTS_KEY = "all_products";
    private static final String CATEGORIES_KEY = "categories";
    private static final String CATEGORY_MAP_KEY = "category_map";
    
    // 获取缓存数据
    private <T> T getCachedData(String key) {
        Map<String, Object> cacheItem = (Map<String, Object>) CACHE.get(key);
        if (cacheItem != null) {
            long timestamp = (long) cacheItem.get("timestamp");
            if (System.currentTimeMillis() - timestamp < CACHE_EXPIRE_TIME) {
                return (T) cacheItem.get("data");
            } else {
                // 缓存过期，移除
                CACHE.remove(key);
            }
        }
        return null;
    }
    
    // 设置缓存数据
    private void setCachedData(String key, Object data) {
        Map<String, Object> cacheItem = new HashMap<>();
        cacheItem.put("timestamp", System.currentTimeMillis());
        cacheItem.put("data", data);
        CACHE.put(key, cacheItem);
    }

    public List<Product> getAllProducts() {
        // 清除缓存，强制从数据库中查询最新的数据
        CACHE.remove(ALL_PRODUCTS_KEY);
        
        System.out.println("=== ProductService: Using native SQL to get all products ===");
        
        // 使用原生SQL查询获取商品数据
        List<Object[]> rawProducts = productRepository.findAllProducts();
        System.out.println("=== Found " + rawProducts.size() + " products in database ===");
        
        // 将查询结果转换为Product对象
        List<Product> products = new java.util.ArrayList<>();
        for (int i = 0; i < rawProducts.size(); i++) {
            Object[] rawProduct = rawProducts.get(i);
            Product product = new Product();

            // 添加调试信息，打印rawProduct数组的长度和各个元素的值
            System.out.println("=== Product " + (i+1) + " rawProduct length: " + rawProduct.length);
            for (int j = 0; j < rawProduct.length; j++) {
                System.out.println("=== Product " + (i+1) + " rawProduct[" + j + "]: " + (rawProduct[j] != null ? rawProduct[j].toString() : "null"));
            }

            // 检查字段数量是否足够
            if (rawProduct.length < 13) {
                System.out.println("=== Product " + (i+1) + " has insufficient fields (" + rawProduct.length + "), skipping ===");
                continue;
            }
            
            // 实际表结构（根据数据库查询结果）：
            // 0: name, 1: weight, 2: packet, 3: variety, 4: grade, 5: specification_desc
            // 6: price, 7: order_quantity, 8: heat, 9: shop, 10: address, 11: image_url, 12: product_code
            
            // 获取商品名称（索引0）
            String productName = rawProduct[0] != null ? rawProduct[0].toString().trim() : "";
            
            // 检查商品名称是否为空，如果为空则跳过该商品
            if (productName.isEmpty()) {
                System.out.println("=== Product " + (i+1) + " has empty name, skipping ===");
                continue;
            }

            // 使用数据库中的真实product_code（索引12）
            Long realProductCode;
            try {
                realProductCode = rawProduct[12] != null ? Long.parseLong(rawProduct[12].toString()) : (long) i;
            } catch (NumberFormatException e) {
                System.out.println("=== Product " + (i+1) + " invalid product_code: " + rawProduct[12] + ", using index ===");
                realProductCode = (long) i;
            }
            product.setProductCode(realProductCode);

            // 设置基本字段
            product.setName(productName);
            // price 在索引6
            product.setPrice(rawProduct[6] != null ? rawProduct[6].toString() : "0");
            // image_url 在索引11
            product.setImageUrl(rawProduct[11] != null ? rawProduct[11].toString() : "");
            // specification_desc 在索引5
            product.setSpecName(rawProduct[5] != null ? rawProduct[5].toString() : "默认规格");
            
            // 设置其他可选字段
            // address 在索引10
            product.setAddress(rawProduct[10] != null ? rawProduct[10].toString() : "");
            // variety 在索引3
            product.setVariety(rawProduct[3] != null ? rawProduct[3].toString() : "");
            // grade 在索引4
            product.setGrade(rawProduct[4] != null ? rawProduct[4].toString() : "");
            // shop 在索引9
            product.setShop(rawProduct[9] != null ? rawProduct[9].toString() : "");
            // heat 在索引8
            product.setHeat(rawProduct[8] != null ? rawProduct[8].toString() : "0");

            // 打印最终映射结果
            System.out.println("=== Product " + (i+1) + " Final Mapping ===");
            System.out.println("=== Name: " + product.getName());
            System.out.println("=== Spec: " + product.getSpecName());
            System.out.println("=== Price: " + product.getPrice());
            System.out.println("=== Variety: " + product.getVariety());
            System.out.println("=== Grade: " + product.getGrade());
            System.out.println("=== Shop: " + product.getShop());
            System.out.println("=== Address: " + product.getAddress());
            System.out.println("=== ImageUrl: " + product.getImageUrl());

            products.add(product);
        }

        // 缓存商品数据
        setCachedData(ALL_PRODUCTS_KEY, products);

        // 调试：打印第一条记录
        if (!products.isEmpty()) {
            Product first = products.get(0);
            System.out.println("=== DEBUG First Product ===");
            System.out.println("name: " + first.getName());
            System.out.println("price: " + first.getPrice());
            System.out.println("imageUrl: " + first.getImageUrl());
        }

        return products;
    }
    
    // 移除去重逻辑，直接返回所有商品数据
    public List<Product> getProducts() {
        return getAllProducts();
    }
    
    /**
     * 根据商品编码获取商品
     * 注意：这里的id是前端传来的商品编码
     */
    public Optional<Product> getProductById(Long id) {
        System.out.println("=== ProductService: getProductById called with id=" + id);
        if (id == null) {
            System.out.println("=== ProductService: id is null, returning empty");
            return Optional.empty();
        }
        
        // 从getAllProducts()获取的所有商品中查找对应ID的商品
        // 这样可以确保字段映射正确，因为getAllProducts()使用手动字段映射
        try {
            List<Product> allProducts = getAllProducts();
            for (Product product : allProducts) {
                if (product.getProductCode() != null && product.getProductCode().equals(id)) {
                    System.out.println("=== ProductService: Found product with code=" + id + ", name=" + product.getName());
                    return Optional.of(product);
                }
            }
            System.out.println("=== ProductService: Product not found with code=" + id);
        } catch (Exception e) {
            System.out.println("=== ProductService: Error finding product with code=" + id + ": " + e.getMessage());
            e.printStackTrace();
        }
        
        return Optional.empty();
    }
    
    /**
     * 根据商品名称获取所有规格的商品
     */
    public List<Product> getProductsByName(String name) {
        List<Product> allProducts = getAllProducts();
        List<Product> productsByName = new java.util.ArrayList<>();
        for (Product product : allProducts) {
            if (product.getName().equals(name)) {
                productsByName.add(product);
            }
        }
        return productsByName;
    }


}
