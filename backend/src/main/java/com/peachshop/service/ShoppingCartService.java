package com.peachshop.service;

import com.peachshop.model.ShoppingCart;
import com.peachshop.model.Product;
import com.peachshop.repository.ShoppingCartRepository;
import com.peachshop.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ShoppingCartService {

    @Autowired
    private ShoppingCartRepository shoppingCartRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private ProductService productService;

    public List<Map<String, Object>> getAllShoppingCartItems() {
        System.out.println("=== ShoppingCartService: Using native SQL to get all shopping cart items ===");
        
        // 使用原生SQL查询获取购物车数据
        List<Object[]> rawItems = shoppingCartRepository.findAllShoppingCartItems();
        System.out.println("=== Found " + rawItems.size() + " shopping cart items in database ===");
        
        // 将查询结果转换为包含商品详情的Map对象
        List<Map<String, Object>> items = new ArrayList<>();
        for (int i = 0; i < rawItems.size(); i++) {
            Object[] rawItem = rawItems.get(i);
            Map<String, Object> itemMap = new HashMap<>();
            
            // 为每个购物车项目设置属性，按照数据库表结构的顺序：cart_id, user_id, product_code, product_num, is_valid, create_time, update_time, is_checked
            // 注意：根据截图，shopping_cart表的字段顺序是：cart_id, user_id, product_code, product_num, is_valid, create_time, update_time, is_checked
            if (rawItem.length >= 8) {
                Long cartId = rawItem[0] != null ? Long.parseLong(rawItem[0].toString()) : null;
                Long userId = rawItem[1] != null ? Long.parseLong(rawItem[1].toString()) : null;
                Long productCode = rawItem[2] != null ? Long.parseLong(rawItem[2].toString()) : null;
                Integer productNum = rawItem[3] != null ? Integer.parseInt(rawItem[3].toString()) : 0;
                Integer isValid = rawItem[4] != null ? Integer.parseInt(rawItem[4].toString()) : 1;
                // rawItem[5]是create_time, rawItem[6]是update_time
                Integer isChecked = rawItem[7] != null ? Integer.parseInt(rawItem[7].toString()) : 1;

                // 设置购物车基本信息
                itemMap.put("cartId", cartId);
                itemMap.put("userId", userId);
                itemMap.put("productCode", productCode != null ? productCode.toString() : "");
                itemMap.put("productNum", productNum);
                itemMap.put("isValid", isValid);
                itemMap.put("isChecked", isChecked);

                // 尝试获取商品详情
                try {
                    if (productCode != null) {
                        // 使用ProductService获取商品详情，避免字段映射问题
                        Optional<Product> productOpt = productService.getProductById(productCode);
                        if (productOpt.isPresent()) {
                            Product product = productOpt.get();
                            // 设置商品详情
                            itemMap.put("productName", product.getName());
                            itemMap.put("productImage", product.getImageUrl());
                            itemMap.put("productPrice", extractPrice(product.getPrice())); // 添加商品价格
                            itemMap.put("productWeight", product.getWeight());
                            itemMap.put("productGrade", product.getGrade());
                            itemMap.put("productShop", product.getShop());
                            itemMap.put("productAddress", product.getAddress());
                            // 添加规格信息
                            itemMap.put("productSpec", product.getSpecName());
                            itemMap.put("productPacket", product.getPacket());
                            itemMap.put("productVariety", product.getVariety());
                        }
                    }
                } catch (Exception e) {
                    System.out.println("=== ShoppingCartService: Error fetching product details: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            items.add(itemMap);
        }
        
        // 调试：打印第一条记录
        if (!items.isEmpty()) {
            Map<String, Object> first = items.get(0);
            System.out.println("=== DEBUG First Shopping Cart Item ===");
            System.out.println("cartId: " + first.get("cartId"));
            System.out.println("userId: " + first.get("userId"));
            System.out.println("productCode: " + first.get("productCode"));
            System.out.println("productNum: " + first.get("productNum"));
            System.out.println("productPrice: " + first.get("productPrice"));
            System.out.println("productName: " + first.get("productName"));
            System.out.println("productImage: " + first.get("productImage"));
        }
        
        return items;
    }
    
    /**
     * 根据用户ID获取购物车项目
     */
    public List<Map<String, Object>> getShoppingCartItemsByUserId(Long userId) {
        System.out.println("=== ShoppingCartService: Getting shopping cart items for user " + userId + " ===");
        
        // 使用原生SQL查询获取指定用户的购物车数据
        List<Object[]> rawItems = shoppingCartRepository.findByUserId(userId);
        System.out.println("=== Found " + rawItems.size() + " shopping cart items for user " + userId + " ===");
        
        // 将查询结果转换为包含商品详情的Map对象
        List<Map<String, Object>> items = new ArrayList<>();
        for (int i = 0; i < rawItems.size(); i++) {
            Object[] rawItem = rawItems.get(i);
            Map<String, Object> itemMap = new HashMap<>();
            
            // 为每个购物车项目设置属性，按照数据库表结构的顺序：cart_id, user_id, product_code, product_num, is_valid, create_time, update_time, is_checked
            // 注意：根据截图，shopping_cart表的字段顺序是：cart_id, user_id, product_code, product_num, is_valid, create_time, update_time, is_checked
            if (rawItem.length >= 8) {
                Long cartId = rawItem[0] != null ? Long.parseLong(rawItem[0].toString()) : null;
                Long itemUserId = rawItem[1] != null ? Long.parseLong(rawItem[1].toString()) : null;
                Long productCode = rawItem[2] != null ? Long.parseLong(rawItem[2].toString()) : null;
                Integer productNum = rawItem[3] != null ? Integer.parseInt(rawItem[3].toString()) : 0;
                Integer isValid = rawItem[4] != null ? Integer.parseInt(rawItem[4].toString()) : 1;
                // rawItem[5]是create_time, rawItem[6]是update_time
                Integer isChecked = rawItem[7] != null ? Integer.parseInt(rawItem[7].toString()) : 1;
                
                // 设置购物车基本信息
                itemMap.put("cartId", cartId);
                itemMap.put("userId", itemUserId);
                itemMap.put("productCode", productCode != null ? productCode.toString() : "");
                itemMap.put("productNum", productNum);
                itemMap.put("isValid", isValid);
                itemMap.put("isChecked", isChecked);
                
                // 尝试获取商品详情
                try {
                    if (productCode != null) {
                        // 使用ProductService获取商品详情，避免字段映射问题
                        Optional<Product> productOpt = productService.getProductById(productCode);
                        if (productOpt.isPresent()) {
                            Product product = productOpt.get();
                            // 设置商品详情
                            itemMap.put("productName", product.getName());
                            itemMap.put("productImage", product.getImageUrl());
                            itemMap.put("productPrice", extractPrice(product.getPrice())); // 添加商品价格
                            itemMap.put("productWeight", product.getWeight());
                            itemMap.put("productGrade", product.getGrade());
                            itemMap.put("productShop", product.getShop());
                            itemMap.put("productAddress", product.getAddress());
                            // 添加规格信息
                            itemMap.put("productSpec", product.getSpecName());
                            itemMap.put("productPacket", product.getPacket());
                            itemMap.put("productVariety", product.getVariety());
                        }
                    }
                } catch (Exception e) {
                    System.out.println("=== ShoppingCartService: Error fetching product details: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            items.add(itemMap);
            
            // 调试：打印每个购物车项的信息
            System.out.println("=== ShoppingCart Item: cartId=" + itemMap.get("cartId") + 
                ", productCode=" + itemMap.get("productCode") + 
                ", productName=" + itemMap.get("productName") + 
                ", productPrice=" + itemMap.get("productPrice") + 
                ", productSpec=" + itemMap.get("productSpec") + 
                ", productVariety=" + itemMap.get("productVariety"));
        }
        
        System.out.println("=== Total shopping cart items returned: " + items.size());
        
        return items;
    }
    
    /**
     * 根据用户ID和商品代码获取购物车项目
     * 注意：由于每个规格是独立的商品记录，使用product_code即可区分不同规格
     */
    public Optional<ShoppingCart> getShoppingCartItemByUserIdAndProductCode(Long userId, Long productCode) {
        System.out.println("=== ShoppingCartService: Getting shopping cart item for user " + userId + ", product code " + productCode + " ===");

        // 使用原生SQL查询获取指定用户和商品的购物车数据
        Object[] rawItem = shoppingCartRepository.findByUserIdAndProductCodeForCheck(userId, productCode);

        if (rawItem != null && rawItem.length >= 8) {
            ShoppingCart item = new ShoppingCart();
            // 按照数据库表结构的顺序：cart_id, user_id, product_code, product_num, is_valid, create_time, update_time, is_checked
            item.setCartId(rawItem[0] != null ? Long.parseLong(rawItem[0].toString()) : null);
            item.setUserId(rawItem[1] != null ? Long.parseLong(rawItem[1].toString()) : null);
            item.setProductCode(rawItem[2] != null ? Long.parseLong(rawItem[2].toString()) : null);
            item.setProductNum(rawItem[3] != null ? Integer.parseInt(rawItem[3].toString()) : 0);
            // rawItem[4]是is_valid, rawItem[5]是create_time, rawItem[6]是update_time, rawItem[7]是is_checked
            return Optional.of(item);
        }

        return Optional.empty();
    }
    
    /**
     * 根据ID获取购物车项目
     */
    public Optional<ShoppingCart> getShoppingCartItemById(Long cartId) {
        return shoppingCartRepository.findById(cartId);
    }
    
    /**
     * 添加购物车项目
     */
    public ShoppingCart addShoppingCartItem(ShoppingCart item) {
        return shoppingCartRepository.save(item);
    }
    
    /**
     * 更新购物车项目
     */
    public ShoppingCart updateShoppingCartItem(ShoppingCart item) {
        return shoppingCartRepository.save(item);
    }
    
    /**
     * 删除购物车项目
     */
    public void deleteShoppingCartItem(Long cartId) {
        shoppingCartRepository.deleteById(cartId);
    }
    
    /**
     * 获取购物车项目总数
     */
    public Long getShoppingCartItemCount() {
        return shoppingCartRepository.countShoppingCartItems();
    }
    
    /**
     * 从价格字符串中提取数字，例如从"1.7元/斤"中提取1.7
     */
    private Double extractPrice(String priceStr) {
        if (priceStr == null || priceStr.isEmpty()) {
            return 0.0;
        }
        
        // 使用正则表达式提取价格数字
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d+\\.\\d+|\\d+)");
        java.util.regex.Matcher matcher = pattern.matcher(priceStr);
        
        if (matcher.find()) {
            try {
                return Double.parseDouble(matcher.group(1));
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
        return 0.0;
    }
}
