package com.peachshop.controller;

import com.peachshop.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recommendation")
public class RecommendationController {

    @Autowired
    private RecommendationService recommendationService;

    @GetMapping("/user/{userId}")
    public Mono<Map<String, Object>> getUserRecommendations(@PathVariable int userId) {
        return recommendationService.getRecommendations(userId)
                .map(products -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("code", 0);
                    response.put("msg", "success");
                    response.put("data", products);
                    return response;
                })
                .onErrorResume(error -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("code", -1);
                    response.put("msg", "Failed to get recommendations: " + error.getMessage());
                    response.put("data", null);
                    return Mono.just(response);
                });
    }

    @GetMapping("/popular")
    public Mono<Map<String, Object>> getPopularItems() {
        return recommendationService.getPopularItems()
                .map(products -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("code", 0);
                    response.put("msg", "success");
                    response.put("data", products);
                    return response;
                })
                .onErrorResume(error -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("code", -1);
                    response.put("msg", "Failed to get popular items: " + error.getMessage());
                    response.put("data", null);
                    return Mono.just(response);
                });
    }
}
