package com.peachshop.controller;

import com.peachshop.model.Product;
import com.peachshop.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/database")
public class DatabaseController {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private ProductRepository productRepository;

    @GetMapping("/health")
    public Map<String, Object> databaseHealth() {
        try {
            // 尝试执行一个简单的SQL查询
            String result = jdbcTemplate.queryForObject("SELECT version()", String.class);
            String message = "Database connection successful! PostgreSQL version: " + result;
            
            // 构建符合前端期望的格式：{ code: number, msg: string, data: any }
            Map<String, Object> response = new HashMap<>();
            response.put("code", 0);
            response.put("msg", "success");
            response.put("data", message);
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", -1);
            response.put("msg", "Database connection failed: " + e.getMessage());
            response.put("data", null);
            return response;
        }
    }

    @GetMapping("/test")
    public Map<String, Object> testConnection() {
        try {
            // 尝试获取数据库连接
            jdbcTemplate.getDataSource().getConnection().close();
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 0);
            response.put("msg", "success");
            response.put("data", "Database connection is OK!");
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", -1);
            response.put("msg", "Database connection error: " + e.getMessage());
            response.put("data", null);
            return response;
        }
    }
    
    @GetMapping("/count")
    public Map<String, Object> countProducts() {
        try {
            // 计算products表中的记录数
            long count = productRepository.count();
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 0);
            response.put("msg", "success");
            response.put("data", "Total products in database: " + count);
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", -1);
            response.put("msg", "Failed to count products: " + e.getMessage());
            response.put("data", null);
            return response;
        }
    }
    
    @GetMapping("/products")
    public Map<String, Object> getAllProducts() {
        try {
            List<Product> products = productRepository.findAll();
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 0);
            response.put("msg", "success");
            response.put("data", products);
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", -1);
            response.put("msg", "Failed to get products: " + e.getMessage());
            response.put("data", null);
            return response;
        }
    }

}