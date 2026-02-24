package com.peachshop.controller;

import com.peachshop.model.ShoppingCart;
import com.peachshop.service.ShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 获取所有购物车项目
     */
    @GetMapping("/shopping-cart")
    public ResponseEntity<Map<String, Object>> getAllShoppingCartItems() {
        List<Map<String, Object>> items = shoppingCartService.getAllShoppingCartItems();
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("msg", "success");
        response.put("data", items);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 根据用户ID获取购物车项目
     */
    @GetMapping("/shopping-cart/user/{userId}")
    public ResponseEntity<Map<String, Object>> getShoppingCartItemsByUserId(@PathVariable Long userId) {
        List<Map<String, Object>> items = shoppingCartService.getShoppingCartItemsByUserId(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("msg", "success");
        response.put("data", items);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 根据ID获取购物车项目
     */
    @GetMapping("/shopping-cart/{id}")
    public ResponseEntity<Map<String, Object>> getShoppingCartItemById(@PathVariable Long id) {
        Optional<ShoppingCart> itemOpt = shoppingCartService.getShoppingCartItemById(id);
        
        Map<String, Object> response = new HashMap<>();
        if (itemOpt.isPresent()) {
            response.put("code", 0);
            response.put("msg", "success");
            response.put("data", itemOpt.get());
        } else {
            response.put("code", 404);
            response.put("msg", "Shopping cart item not found");
            response.put("data", null);
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 添加购物车项目
     */
    @PostMapping("/shopping-cart")
    public ResponseEntity<Map<String, Object>> addShoppingCartItem(@RequestBody ShoppingCart item) {
        ShoppingCart savedItem = shoppingCartService.addShoppingCartItem(item);
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("msg", "success");
        response.put("data", savedItem);
        
        return ResponseEntity.ok(response);
    }

    /**
        * 加入购物车（用于前端调用）
        */
       @PostMapping("/shopping-cart/add")
       public ResponseEntity<Map<String, Object>> addToShoppingCart(@RequestBody Map<String, Object> request) {
           System.out.println("=== ShoppingCartController: addToShoppingCart called with request: " + request);
           try {
               // 检查request是否包含必要的字段
               if (!request.containsKey("userId") || !request.containsKey("productCode")) {
                   System.out.println("=== ShoppingCartController: Missing required fields");
                   Map<String, Object> response = new HashMap<>();
                   response.put("code", 400);
                   response.put("msg", "Missing required fields: userId and productCode are required");
                   response.put("data", null);
                   return ResponseEntity.ok(response);
               }
               
               // 打印所有请求字段及其类型
               for (String key : request.keySet()) {
                   Object value = request.get(key);
                   System.out.println("=== ShoppingCartController: Request field: " + key + " = " + value + " (type: " + (value != null ? value.getClass().getName() : "null") + ")");
               }
               
               Long userId = Long.parseLong(request.get("userId").toString());
               Long productCode = Long.parseLong(request.get("productCode").toString());
               Integer productNum = request.get("productNum") != null ? Integer.parseInt(request.get("productNum").toString()) : 1;
               Double productPrice = request.get("productPrice") != null ? Double.parseDouble(request.get("productPrice").toString()) : 0.0;
               String productSpec = request.get("productSpec") != null ? request.get("productSpec").toString() : "";
               
               System.out.println("=== ShoppingCartController: Processing add to cart for user " + userId + ", productCode " + productCode + ", productNum " + productNum + ", productPrice " + productPrice + ", productSpec " + productSpec);
               
               // 检查是否已存在相同商品（由于每个规格是独立的商品记录，使用product_code即可区分）
               Optional<ShoppingCart> existingItemOpt = shoppingCartService.getShoppingCartItemByUserIdAndProductCode(userId, productCode);

               if (existingItemOpt.isPresent()) {
                   // 已存在相同商品，更新数量
                   System.out.println("=== ShoppingCartController: Existing item found, updating quantity");
                   ShoppingCart existingItem = existingItemOpt.get();
                   existingItem.setProductNum(existingItem.getProductNum() + productNum);
                   existingItem.setUpdateTime(LocalDateTime.now());
                   ShoppingCart updatedItem = shoppingCartService.updateShoppingCartItem(existingItem);
                   System.out.println("=== ShoppingCartController: Updated existing item: " + updatedItem);

                   Map<String, Object> response = new HashMap<>();
                   response.put("code", 0);
                   response.put("msg", "success");
                   response.put("data", updatedItem);

                   return ResponseEntity.ok(response);
               } else {
                   // 不存在相同商品，创建新商品
                   System.out.println("=== ShoppingCartController: Creating new shopping cart item");
                   ShoppingCart newItem = new ShoppingCart();
                   newItem.setUserId(userId);
                   newItem.setProductCode(productCode);
                   // productId与productCode相同
                   newItem.setProductId(productCode);
                   newItem.setProductNum(productNum);
                   // 设置is_checked为默认值1（已选中）
                   newItem.setIsChecked(1);
                   // 设置productPrice
                   newItem.setProductPrice(productPrice);
                   // 注意：规格信息通过product_code区分（每个规格是独立的商品记录）
                   // 设置create_time和update_time为当前时间
                   LocalDateTime now = LocalDateTime.now();
                   newItem.setCreateTime(now);
                   newItem.setUpdateTime(now);
                   System.out.println("=== ShoppingCartController: Creating new item: " + newItem);
                   ShoppingCart savedItem = shoppingCartService.addShoppingCartItem(newItem);
                   System.out.println("=== ShoppingCartController: Created new item: " + savedItem);
                   
                   System.out.println("=== ShoppingCartController: Item added to cart successfully: " + savedItem);
                   
                   Map<String, Object> response = new HashMap<>();
                   response.put("code", 0);
                   response.put("msg", "success");
                   response.put("data", savedItem);
                   
                   return ResponseEntity.ok(response);
               }
           } catch (Exception e) {
               System.out.println("=== ShoppingCartController: Error adding item to cart: " + e.getMessage());
               e.printStackTrace();
               Map<String, Object> response = new HashMap<>();
               response.put("code", 500);
               response.put("msg", "Failed to add item to shopping cart: " + e.getMessage());
               response.put("data", null);
               
               return ResponseEntity.ok(response);
           }
       }

    /**
     * 更新购物车项目
     */
    @PutMapping("/shopping-cart/{id}")
    public ResponseEntity<Map<String, Object>> updateShoppingCartItem(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        Optional<ShoppingCart> existingItemOpt = shoppingCartService.getShoppingCartItemById(id);
        
        Map<String, Object> response = new HashMap<>();
        if (existingItemOpt.isPresent()) {
            ShoppingCart existingItem = existingItemOpt.get();
            
            // 只更新请求中提供的字段
            if (request.containsKey("productNum")) {
                existingItem.setProductNum(Integer.parseInt(request.get("productNum").toString()));
            }
            if (request.containsKey("isChecked")) {
                existingItem.setIsChecked(Integer.parseInt(request.get("isChecked").toString()));
            }
            if (request.containsKey("productPrice")) {
                existingItem.setProductPrice(Double.parseDouble(request.get("productPrice").toString()));
            }
            
            // 更新时间戳
            existingItem.setUpdateTime(java.time.LocalDateTime.now());
            
            ShoppingCart updatedItem = shoppingCartService.updateShoppingCartItem(existingItem);
            response.put("code", 0);
            response.put("msg", "success");
            response.put("data", updatedItem);
        } else {
            response.put("code", 404);
            response.put("msg", "Shopping cart item not found");
            response.put("data", null);
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 删除购物车项目
     */
    @DeleteMapping("/shopping-cart/{id}")
    public ResponseEntity<Map<String, Object>> deleteShoppingCartItem(@PathVariable Long id) {
        Optional<ShoppingCart> existingItemOpt = shoppingCartService.getShoppingCartItemById(id);
        
        Map<String, Object> response = new HashMap<>();
        if (existingItemOpt.isPresent()) {
            shoppingCartService.deleteShoppingCartItem(id);
            response.put("code", 0);
            response.put("msg", "success");
            response.put("data", null);
        } else {
            response.put("code", 404);
            response.put("msg", "Shopping cart item not found");
            response.put("data", null);
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取购物车项目总数
     */
    @GetMapping("/shopping-cart/count")
    public ResponseEntity<Map<String, Object>> getShoppingCartItemCount() {
        Long count = shoppingCartService.getShoppingCartItemCount();
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("msg", "success");
        response.put("data", count);
        
        return ResponseEntity.ok(response);
    }
}
