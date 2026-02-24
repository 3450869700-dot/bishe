package com.peachshop.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/shop/goods")
public class GoodsReputationController {

    /**
     * 获取商品评价列表
     */
    @PostMapping("/reputation/v2")
    public ResponseEntity<Map<String, Object>> getGoodsReputation(@RequestBody Map<String, Object> data) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("msg", "success");
        response.put("data", new HashMap<String, Object>() {{ 
            put("list", new java.util.ArrayList<>()); // 返回空评价列表
            put("total", 0);
            put("score", 5.0); // 默认评分
        }});
        return ResponseEntity.ok(response);
    }

    /**
     * 获取商品会员折扣
     */
    @GetMapping("/rebate/v2")
    public ResponseEntity<Map<String, Object>> getGoodsRebate(@RequestParam Map<String, Object> params) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("msg", "success");
        response.put("data", new HashMap<>()); // 返回空对象
        return ResponseEntity.ok(response);
    }
}
