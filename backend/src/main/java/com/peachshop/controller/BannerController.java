package com.peachshop.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/banner")
public class BannerController {

    @GetMapping("/list")
    public Map<String, Object> bannerList() {
        // 创建模拟的banner数据
        List<Map<String, Object>> bannerList = new ArrayList<>();
        
        // 添加一些模拟banner数据
        Map<String, Object> banner1 = new HashMap<>();
        banner1.put("id", 1);
        banner1.put("title", "夏日水蜜桃促销");
        banner1.put("picUrl", "https://image.cnhnb.com/image/jpg/2023/09/11/a3b22e8272c840a398bafdbab5312b3a.jpg?x-tos-process=image/resize,m_fill,w_1200,h_400/interlace,1/quality,q_100/format,jpg");
        banner1.put("linkUrl", "#");
        bannerList.add(banner1);
        
        Map<String, Object> banner2 = new HashMap<>();
        banner2.put("id", 2);
        banner2.put("title", "新鲜黄桃上市");
        banner2.put("picUrl", "https://image.cnhnb.com/image/jpg/2023/09/11/2a49d4c6167944cea9b749ff82deb6eb.jpg?x-tos-process=image/resize,m_fill,w_1200,h_400/interlace,1/quality,q_100/format,jpg");
        banner2.put("linkUrl", "#");
        bannerList.add(banner2);
        
        Map<String, Object> banner3 = new HashMap<>();
        banner3.put("id", 3);
        banner3.put("title", "鹰嘴桃特惠");
        banner3.put("picUrl", "https://image.cnhnb.com/change/image/tos/pro/jpg/details/2024/04/12/bb8f763217ea4f8cb992984b1318a926.jpg?x-tos-process=image/resize,m_fill,w_1200,h_400/interlace,1/quality,q_100/format,jpg");
        banner3.put("linkUrl", "#");
        bannerList.add(banner3);
        
        // 构建符合前端期望的响应格式
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("msg", "success");
        response.put("data", bannerList);
        
        return response;
    }
    
    @GetMapping("/types")
    public Map<String, Object> bannerTypes() {
        // 返回banner类型
        List<Map<String, Object>> types = new ArrayList<>();
        
        Map<String, Object> type1 = new HashMap<>();
        type1.put("id", "indexBanner");
        type1.put("name", "首页轮播图");
        types.add(type1);
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("msg", "success");
        response.put("data", types);
        
        return response;
    }
}
