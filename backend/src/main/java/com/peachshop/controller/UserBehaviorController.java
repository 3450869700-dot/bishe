package com.peachshop.controller;

import com.peachshop.service.UserBehaviorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户行为数据收集Controller
 * 提供前端调用的行为记录API
 */
@RestController
@RequestMapping("/api/behavior")
public class UserBehaviorController {

    @Autowired
    private UserBehaviorService userBehaviorService;

    /**
     * 记录浏览行为
     * POST /api/behavior/view
     * Body: { userId: 1, productId: 123, stayDuration: 30 }
     */
    @PostMapping("/view")
    public ResponseEntity<Map<String, Object>> recordView(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Integer userId = (Integer) request.get("userId");
            Long productId = Long.valueOf(request.get("productId").toString());
            Integer stayDuration = request.get("stayDuration") != null ? 
                (Integer) request.get("stayDuration") : 0;
            
            if (userId == null || productId == null) {
                response.put("code", 400);
                response.put("msg", "userId and productId are required");
                return ResponseEntity.badRequest().body(response);
            }
            
            userBehaviorService.recordView(userId, productId, stayDuration);
            
            response.put("code", 0);
            response.put("msg", "success");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("code", 500);
            response.put("msg", "Error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 记录收藏行为
     * POST /api/behavior/favorite
     * Body: { userId: 1, productId: 123 }
     */
    @PostMapping("/favorite")
    public ResponseEntity<Map<String, Object>> recordFavorite(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Integer userId = (Integer) request.get("userId");
            Long productId = Long.valueOf(request.get("productId").toString());
            
            if (userId == null || productId == null) {
                response.put("code", 400);
                response.put("msg", "userId and productId are required");
                return ResponseEntity.badRequest().body(response);
            }
            
            userBehaviorService.recordFavorite(userId, productId);
            
            response.put("code", 0);
            response.put("msg", "success");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("code", 500);
            response.put("msg", "Error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 记录加购行为
     * POST /api/behavior/cart
     * Body: { userId: 1, productId: 123 }
     */
    @PostMapping("/cart")
    public ResponseEntity<Map<String, Object>> recordCart(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Integer userId = (Integer) request.get("userId");
            Long productId = Long.valueOf(request.get("productId").toString());
            
            if (userId == null || productId == null) {
                response.put("code", 400);
                response.put("msg", "userId and productId are required");
                return ResponseEntity.badRequest().body(response);
            }
            
            userBehaviorService.recordCart(userId, productId);
            
            response.put("code", 0);
            response.put("msg", "success");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("code", 500);
            response.put("msg", "Error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 记录购买行为
     * POST /api/behavior/purchase
     * Body: { userId: 1, productId: 123 }
     */
    @PostMapping("/purchase")
    public ResponseEntity<Map<String, Object>> recordPurchase(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Integer userId = (Integer) request.get("userId");
            Long productId = Long.valueOf(request.get("productId").toString());
            
            if (userId == null || productId == null) {
                response.put("code", 400);
                response.put("msg", "userId and productId are required");
                return ResponseEntity.badRequest().body(response);
            }
            
            userBehaviorService.recordPurchase(userId, productId);
            
            response.put("code", 0);
            response.put("msg", "success");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("code", 500);
            response.put("msg", "Error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 记录搜索行为
     * POST /api/behavior/search
     * Body: { userId: 1, keyword: "苹果" }
     */
    @PostMapping("/search")
    public ResponseEntity<Map<String, Object>> recordSearch(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Integer userId = (Integer) request.get("userId");
            String keyword = (String) request.get("keyword");
            
            if (userId == null || keyword == null || keyword.isEmpty()) {
                response.put("code", 400);
                response.put("msg", "userId and keyword are required");
                return ResponseEntity.badRequest().body(response);
            }
            
            userBehaviorService.recordSearch(userId, keyword);
            
            response.put("code", 0);
            response.put("msg", "success");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("code", 500);
            response.put("msg", "Error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 获取用户行为统计
     * GET /api/behavior/stats?userId=1
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getUserStats(@RequestParam Integer userId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Object> stats = userBehaviorService.getUserBehaviorStats(userId);
            
            response.put("code", 0);
            response.put("msg", "success");
            response.put("data", stats);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("code", 500);
            response.put("msg", "Error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
