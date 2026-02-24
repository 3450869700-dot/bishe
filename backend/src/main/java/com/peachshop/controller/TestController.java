package com.peachshop.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class TestController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping(value = "/test/encoding", produces = MediaType.APPLICATION_JSON_VALUE)
    public String testEncoding() {
        return "{\"message\": \"阳山水蜜桃中文测试\"}";
    }
    
    @GetMapping(value = "/test/table-structure", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Map<String, Object>> testTableStructure() {
        // 查询products表的结构
        String sql = "SELECT column_name, data_type FROM information_schema.columns WHERE table_name = 'products'";
        return jdbcTemplate.queryForList(sql);
    }
    
    @GetMapping(value = "/test/first-product", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Map<String, Object>> testFirstProduct() {
        // 查询第一条产品数据
        String sql = "SELECT * FROM products LIMIT 1";
        return jdbcTemplate.queryForList(sql);
    }
    
    @GetMapping(value = "/test/simple", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> testSimple() {
        // 返回一个简单的JSON响应，不查询数据库
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Hello from Peach Shop");
        response.put("status", "OK");
        return response;
    }
    
    @GetMapping(value = "/test/products-simple", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> testProductsSimple() {
        // 简单查询数据库并返回结果，不进行复杂处理
        String sql = "SELECT COUNT(*) as count FROM products";
        Map<String, Object> countResult = jdbcTemplate.queryForMap(sql);
        
        Map<String, Object> response = new HashMap<>();
        response.put("count", countResult.get("count"));
        response.put("message", "Products count from database");
        return response;
    }
}
