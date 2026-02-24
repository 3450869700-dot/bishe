package com.peachshop.controller;

import com.peachshop.model.Product;
import com.peachshop.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api")
public class ProductController {

    @Autowired
    private ProductService productService;
    
    // 缓存机制
    private static final ConcurrentHashMap<String, Object> CACHE = new ConcurrentHashMap<>();
    private static final long CACHE_EXPIRE_TIME = 300000; // 缓存过期时间：5分钟
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
    
    /**
     * 从价格字符串中提取数字，例如从"1.7元/斤"中提取1.7
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

    /**
     * 获取所有商品分类
     * 基于商品的发货地址前两个文字进行分类
     */
    @GetMapping("/shop/goods/category/all")
    public ResponseEntity<Map<String, Object>> getAllCategories() {
        Map<String, Object> response = new HashMap<>();

        // 首先检查缓存
        List<Map<String, Object>> cachedCategories = getCachedData(CATEGORIES_KEY);
        if (cachedCategories != null) {
            System.out.println("=== ProductController: Using cached categories ===");
            response.put("code", 0);
            response.put("msg", "success");
            response.put("data", cachedCategories);
            return ResponseEntity.ok(response);
        }

        // 获取所有商品，用于分析地址分类
        List<Product> allProducts = productService.getProducts();

        // 提取完整省份名称作为分类，使用Set去重
        Set<String> addressPrefixes = new HashSet<>();

        for (Product product : allProducts) {
            String address = product.getAddress();
            String province;
            if (address != null && !address.isEmpty()) {
                // 提取省份名称，支持"省"、"自治区"、"直辖市"等
                if (address.contains("省")) {
                    province = address.substring(0, address.indexOf("省") + 1);
                } else if (address.contains("自治区")) {
                    province = address.substring(0, address.indexOf("自治区") + 3);
                } else if (address.contains("直辖市")) {
                    province = address.substring(0, address.indexOf("直辖市") + 3);
                } else if (address.contains("特别行政区")) {
                    province = address.substring(0, address.indexOf("特别行政区") + 5);
                } else if (address.length() >= 2) {
                    // 如果没有找到明确的省份后缀，取前两个字
                    province = address.substring(0, 2);
                } else {
                    province = "其余地址";
                }
            } else {
                province = "其余地址";
            }
            addressPrefixes.add(province);
        }

        // 构建分类列表，不包含"所有商品"分类
        List<Map<String, Object>> categories = new ArrayList<>();

        // 转换为List以便排序
        List<String> sortedPrefixes = new ArrayList<>(addressPrefixes);

        // 移除"其余地址"，将其固定到最后
        sortedPrefixes.remove("其余地址");

        // 按首字母排序
        sortedPrefixes.sort(Comparator.naturalOrder());

        // 添加排序后的分类，从ID=1开始
        int categoryId = 1;
        for (String prefix : sortedPrefixes) {
            Map<String, Object> category = new HashMap<>();
            category.put("id", categoryId++);
            category.put("name", prefix);
            category.put("parentId", 0);
            categories.add(category);
        }

        // 添加"其余地址"分类到最后
        Map<String, Object> otherCategory = new HashMap<>();
        otherCategory.put("id", categoryId);
        otherCategory.put("name", "其余地址");
        otherCategory.put("parentId", 0);
        categories.add(otherCategory);

        // 缓存分类信息
        setCachedData(CATEGORIES_KEY, categories);

        response.put("code", 0);
        response.put("msg", "success");
        response.put("data", categories);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取商品列表
     * 根据前端传递的分类ID和分页参数返回对应页的商品
     * 分类ID对应地址前缀，如果是1则返回所有商品
     */
    @GetMapping("/products")
    public ResponseEntity<Map<String, Object>> getProducts(
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "24") Integer pageSize) {
        
        try {
            System.out.println("=== ProductController: getProducts called with categoryId=" + categoryId + ", page=" + page + ", pageSize=" + pageSize);
            
            // 生成缓存键
            String cacheKey = "products_" + (categoryId != null ? categoryId : "all") + "_" + page + "_" + pageSize;
            
            // 首先检查缓存
            Map<String, Object> cachedResponse = getCachedData(cacheKey);
            if (cachedResponse != null) {
                System.out.println("=== ProductController: Using cached products ===");
                return ResponseEntity.ok(cachedResponse);
            }

            List<Product> allProducts = productService.getProducts();
            System.out.println("=== ProductController: Got " + allProducts.size() + " products from service");
        List<Product> filteredProducts = new ArrayList<>();
        
        // 首先检查分类映射缓存
        Map<Integer, String> categoryIdToPrefix = getCachedData(CATEGORY_MAP_KEY);
        if (categoryIdToPrefix == null) {
            // 获取所有分类，用于查找分类名称（地址前缀）
            List<Product> productsForCategories = productService.getProducts();
            Set<String> addressPrefixes = new HashSet<>();
            
            for (Product product : productsForCategories) {
                String address = product.getAddress();
                String province;
                if (address != null && !address.isEmpty()) {
                    // 提取省份名称，支持"省"、"自治区"、"直辖市"等
                    if (address.contains("省")) {
                        province = address.substring(0, address.indexOf("省") + 1);
                    } else if (address.contains("自治区")) {
                        province = address.substring(0, address.indexOf("自治区") + 3);
                    } else if (address.contains("直辖市")) {
                        province = address.substring(0, address.indexOf("直辖市") + 3);
                    } else if (address.contains("特别行政区")) {
                        province = address.substring(0, address.indexOf("特别行政区") + 5);
                    } else if (address.length() >= 2) {
                        // 如果没有找到明确的省份后缀，取前两个字
                        province = address.substring(0, 2);
                    } else {
                        province = "其余地址";
                    }
                } else {
                    province = "其余地址";
                }
                addressPrefixes.add(province);
            }
            
            // 构建分类ID到地址前缀的映射，与getAllCategories方法逻辑保持一致
            categoryIdToPrefix = new HashMap<>();
            
            // 转换为List以便排序，与getAllCategories方法相同
            List<String> sortedPrefixes = new ArrayList<>(addressPrefixes);
            
            // 移除"其余地址"，将其固定到最后
            sortedPrefixes.remove("其余地址");
            
            // 按首字母排序
            sortedPrefixes.sort(Comparator.naturalOrder());
            
            // 从ID=1开始映射，没有"所有商品"分类
            int tempCategoryId = 1;
            for (String prefix : sortedPrefixes) {
                categoryIdToPrefix.put(tempCategoryId++, prefix);
            }
            
            // 添加"其余地址"到映射
            categoryIdToPrefix.put(tempCategoryId, "其余地址");
            
            // 缓存分类映射
            setCachedData(CATEGORY_MAP_KEY, categoryIdToPrefix);
        }
        
        // 根据分类ID过滤商品
        // 当categoryId为null时返回所有商品（兼容首页请求）
        // 当categoryId不为null时，根据分类ID对应的完整省份名称过滤商品
        if (categoryId == null) {
            // 返回所有商品，兼容首页请求
            filteredProducts = allProducts;
        } else {
            // 根据完整省份名称过滤商品
            String targetProvince = categoryIdToPrefix.get(categoryId);
            if (targetProvince != null) {
                for (Product product : allProducts) {
                    String address = product.getAddress();
                    String productProvince;
                    
                    if (address != null && !address.isEmpty()) {
                        // 提取商品地址的完整省份名称，与分类生成逻辑保持一致
                        if (address.contains("省")) {
                            productProvince = address.substring(0, address.indexOf("省") + 1);
                        } else if (address.contains("自治区")) {
                            productProvince = address.substring(0, address.indexOf("自治区") + 3);
                        } else if (address.contains("直辖市")) {
                            productProvince = address.substring(0, address.indexOf("直辖市") + 3);
                        } else if (address.contains("特别行政区")) {
                            productProvince = address.substring(0, address.indexOf("特别行政区") + 5);
                        } else if (address.length() >= 2) {
                            // 如果没有找到明确的省份后缀，取前两个字
                            productProvince = address.substring(0, 2);
                        } else {
                            productProvince = "其余地址";
                        }
                    } else {
                        productProvince = "其余地址";
                    }
                    
                    if (productProvince.equals(targetProvince)) {
                        filteredProducts.add(product);
                    }
                }
            }
        }
        
        // 转换为前端期望的格式，按商品名称分组
        Map<String, Map<String, Object>> productMap = new HashMap<>();
        
        for (Product product : filteredProducts) {
            String productName = product.getName();
            
            if (productMap.containsKey(productName)) {
                // 商品已存在，添加规格
                Map<String, Object> existingProduct = productMap.get(productName);
                List<Map<String, Object>> specs = (List<Map<String, Object>>) existingProduct.get("specs");
                
                // 创建新规格 - 只包含必要字段
                Map<String, Object> spec = new HashMap<>();
                spec.put("id", product.getProductCode());
                spec.put("specification", product.getSpecName() != null && !product.getSpecName().isEmpty() ? product.getSpecName() : "默认规格");
                spec.put("price", product.getPrice());
                spec.put("price_num", extractPrice(product.getPrice()));
                spec.put("pic", product.getImageUrl());
                
                specs.add(spec);
                
                // 更新最低价格
                    String currentMinPrice = (String) existingProduct.get("minPrice");
                    if (currentMinPrice != null && !currentMinPrice.isEmpty() && product.getPrice() != null && !product.getPrice().isEmpty()) {
                        // 提取价格数字进行比较
                        double currentPriceValue = extractPrice(currentMinPrice);
                        double newPriceValue = extractPrice(product.getPrice());
                        if (newPriceValue < currentPriceValue) {
                            existingProduct.put("minPrice", product.getPrice());
                            existingProduct.put("minPriceNum", newPriceValue);
                        }
                    }
            } else {
                // 新商品，创建商品对象 - 只包含必要字段
                Map<String, Object> formatted = new HashMap<>();
                formatted.put("id", product.getProductCode());
                formatted.put("name", productName);
                formatted.put("pic", product.getImageUrl());
                formatted.put("minPrice", product.getPrice());
                formatted.put("minPriceNum", extractPrice(product.getPrice()));
                formatted.put("price_num", extractPrice(product.getPrice()));
                
                // 创建规格列表
                List<Map<String, Object>> specs = new ArrayList<>();
                Map<String, Object> spec = new HashMap<>();
                spec.put("id", product.getProductCode());
                spec.put("specification", product.getSpecName() != null && !product.getSpecName().isEmpty() ? product.getSpecName() : "默认规格");
                spec.put("price", product.getPrice());
                spec.put("price_num", extractPrice(product.getPrice()));
                spec.put("pic", product.getImageUrl());
                specs.add(spec);
                
                formatted.put("specs", specs);
                productMap.put(productName, formatted);
            }
        }
        
        // 转换为列表
        List<Map<String, Object>> formattedProducts = new ArrayList<>(productMap.values());
        
        // 实现分页逻辑
        int totalProducts = formattedProducts.size();
        int startIndex = page * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalProducts);
        List<Map<String, Object>> paginatedProducts;
        
        if (startIndex >= totalProducts) {
            paginatedProducts = new ArrayList<>();
        } else {
            paginatedProducts = formattedProducts.subList(startIndex, endIndex);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("result", paginatedProducts);
        result.put("totalRow", totalProducts);
        result.put("pageCurrent", page + 1); // 转换为1-based分页返回给前端
        result.put("pageSize", pageSize);
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("msg", "success");
        response.put("data", result);
        
        // 缓存响应
        setCachedData(cacheKey, response);
        
        return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("=== ProductController: Error in getProducts: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 500);
            errorResponse.put("msg", "Error: " + e.getMessage());
            errorResponse.put("data", null);
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 获取商品详情
     */
    @GetMapping("/products/{id}")
    public ResponseEntity<Map<String, Object>> getProductById(@PathVariable Long id) {
        Optional<Product> productOpt = productService.getProductById(id);

        Map<String, Object> response = new HashMap<>();
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            // 获取同名商品的所有规格，让用户可以在不同规格之间选择
            List<Product> allProducts = productService.getProductsByName(product.getName());
            System.out.println("=== getProductById: Found " + allProducts.size() + " specifications for product: " + product.getName());
            
            // 构建商品详情响应对象
            Map<String, Object> productDetail = new HashMap<>();
            
            // 构建图片列表（当前只有一张图片，根据前端格式要求包装成数组）
            List<Map<String, String>> pics = new ArrayList<>();
            if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                Map<String, String> pic = new HashMap<>();
                pic.put("id", "1"); // 使用固定ID，因为数据库中只有一个图片URL
                pic.put("pic", product.getImageUrl());
                pics.add(pic);
            }
            productDetail.put("pics", pics);
            
            // 构建基本信息
            Map<String, Object> basicInfo = new HashMap<>();
            basicInfo.put("id", product.getProductCode());
            basicInfo.put("name", product.getName());
            basicInfo.put("pic", product.getImageUrl()); // 添加商品图片URL
            // 找到最低价格
            String minPrice = allProducts.stream()
                .map(Product::getPrice)
                .filter(price -> price != null && !price.isEmpty())
                .min(Comparator.comparingDouble(this::extractPrice))
                .orElse(product.getPrice());
            basicInfo.put("minPrice", minPrice);
            basicInfo.put("originalPrice", product.getPrice()); // 使用相同价格作为原价
            basicInfo.put("stores", 100); // 使用固定库存
            basicInfo.put("status", 0); // 0表示商品正常
            basicInfo.put("characteristic", product.getVariety() != null ? product.getVariety() : ""); // 使用品种作为特性
            basicInfo.put("shop", product.getShop()); // 添加店铺信息
            basicInfo.put("grade", product.getGrade()); // 添加等级信息
            productDetail.put("basicInfo", basicInfo);
            
            // 构建物流信息
            Map<String, Object> logistics = new HashMap<>();
            logistics.put("isFree", true); // 默认包邮
            productDetail.put("logistics", logistics);
            
            // 构建商品详情内容
            StringBuilder contentBuilder = new StringBuilder();
            
            // 商品名称 - 必须显示
            if (product.getName() != null && !product.getName().isEmpty()) {
                contentBuilder.append("<p>商品名称：" + product.getName() + "</p>");
            }
            
            // 商品价格 - 必须显示
            if (product.getPrice() != null && !product.getPrice().isEmpty()) {
                contentBuilder.append("<p>商品价格：" + product.getPrice() + "</p>");
            }
            
            // 商品产地 - 只在有值时显示
            if (product.getAddress() != null && !product.getAddress().isEmpty()) {
                contentBuilder.append("<p>商品产地：" + product.getAddress() + "</p>");
            }
            
            // 商品店铺 - 只在有值时显示
            if (product.getShop() != null && !product.getShop().isEmpty()) {
                contentBuilder.append("<p>商品店铺：" + product.getShop() + "</p>");
            }
            
            // 商品等级 - 只在有值时显示
            if (product.getGrade() != null && !product.getGrade().isEmpty()) {
                contentBuilder.append("<p>商品等级：" + product.getGrade() + "</p>");
            }
            
            // 商品品种 - 只在有值时显示
            if (product.getVariety() != null && !product.getVariety().isEmpty()) {
                contentBuilder.append("<p>商品品种：" + product.getVariety() + "</p>");
            }
            
            // 商品重量 - 只在有值时显示
            if (product.getWeight() != null && !product.getWeight().isEmpty()) {
                contentBuilder.append("<p>商品重量：" + product.getWeight() + "</p>");
            }
            
            // 商品包装 - 只在有值时显示
            if (product.getPacket() != null && !product.getPacket().isEmpty()) {
                contentBuilder.append("<p>商品包装：" + product.getPacket() + "</p>");
            }
            
            // 商品规格 - 只在有值时显示
            if (product.getSpecName() != null && !product.getSpecName().isEmpty()) {
                contentBuilder.append("<p>商品规格：" + product.getSpecName() + "</p>");
            }
            
            productDetail.put("content", contentBuilder.toString());
            
            // 构建规格列表，包含所有规格
            List<Map<String, Object>> specifications = new ArrayList<>();
            for (Product p : allProducts) {
                Map<String, Object> spec = new HashMap<>();
                spec.put("id", p.getProductCode());
                spec.put("specification", p.getSpecName() != null && !p.getSpecName().isEmpty() ? p.getSpecName() : "默认规格");
                spec.put("specificationDesc", p.getWeight() != null ? "重量：" + p.getWeight() : "");
                spec.put("price", p.getPrice());
                spec.put("stock", 100); // 使用固定库存
                spec.put("weight", p.getWeight() != null ? p.getWeight() : "");
                spec.put("grade", p.getGrade());
                spec.put("shop", p.getShop());
                specifications.add(spec);
            }
            productDetail.put("specifications", specifications);
            
            response.put("code", 0);
            response.put("msg", "success");
            response.put("data", productDetail);
        } else {
            response.put("code", 404);
            response.put("msg", "Product not found");
            response.put("data", null);
        }
        
        return ResponseEntity.ok(response);
    }
}
