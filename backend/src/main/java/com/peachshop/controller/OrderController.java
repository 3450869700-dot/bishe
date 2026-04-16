package com.peachshop.controller;

import com.peachshop.model.Order;
import com.peachshop.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class OrderController {

    @Autowired
    private OrderService orderService;



    /**
     * 获取订单详情
     */
    @GetMapping("/order/detail")
    public ResponseEntity<Map<String, Object>> getOrderDetail(@RequestParam String orderNumber) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (orderNumber == null || orderNumber.isEmpty()) {
                response.put("code", 400);
                response.put("msg", "订单编号不能为空");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            // 查找订单
            Order order = orderService.getOrderByOrderNumber(orderNumber);
            if (order == null) {
                response.put("code", 404);
                response.put("msg", "订单不存在");
                response.put("data", null);
                return ResponseEntity.ok(response);
            }

            // 构建响应数据
            Map<String, Object> orderInfo = new HashMap<>();
            orderInfo.put("id", order.getId());
            orderInfo.put("orderNumber", order.getOrderNumber());
            orderInfo.put("status", order.getStatus());
            orderInfo.put("statusStr", order.getStatusStr());
            orderInfo.put("amount", order.getAmount());
            orderInfo.put("amountReal", order.getAmountReal());
            orderInfo.put("amountLogistics", order.getAmountLogistics());
            orderInfo.put("isNeedLogistics", order.getIsNeedLogistics());
            orderInfo.put("remark", order.getRemark());
            orderInfo.put("dateAdd", order.getDateAdd().toString());
            orderInfo.put("dateClose", order.getDateClose().toString());
            orderInfo.put("paymentTime", order.getPaymentTime() != null ? order.getPaymentTime().toString() : "");
            orderInfo.put("paymentMethod", order.getPaymentMethod());
            orderInfo.put("traceCode", order.getTraceCode());

            // 模拟商品数据
            List<Map<String, Object>> goods = new ArrayList<>();
            Map<String, Object> good1 = new HashMap<>();
            good1.put("goodsId", 1);
            good1.put("goodsName", "阳山水蜜桃");
            good1.put("pic", "https://via.placeholder.com/200x200?text=Peach");
            good1.put("property", "特级");
            good1.put("amountSingle", 50.0);
            good1.put("number", 2);
            goods.add(good1);

            // 模拟物流轨迹
            List<Map<String, Object>> logs = new ArrayList<>();
            Map<String, Object> log1 = new HashMap<>();
            log1.put("status", "订单创建");
            log1.put("time", order.getDateAdd().toString());
            log1.put("desc", "订单已创建，等待支付");
            logs.add(log1);

            if (order.getStatus() >= 1) {
                Map<String, Object> log2 = new HashMap<>();
                log2.put("status", "支付成功");
                log2.put("time", order.getPaymentTime() != null ? order.getPaymentTime().toString() : "");
                log2.put("desc", "订单支付成功，等待发货");
                logs.add(log2);
            }

            // 构建物流信息
            Map<String, Object> logistics = new HashMap<>();
            logistics.put("linkMan", order.getLinkMan());
            logistics.put("mobile", order.getMobile());
            logistics.put("address", order.getAddress());

            Map<String, Object> data = new HashMap<>();
            data.put("orderInfo", orderInfo);
            data.put("goods", goods);
            data.put("logs", logs);
            data.put("logistics", logistics);

            response.put("code", 0);
            response.put("msg", "success");
            response.put("data", data);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.out.println("=== OrderController: 获取订单详情失败: " + e.getMessage());
            e.printStackTrace();

            response.put("code", 500);
            response.put("msg", "获取订单详情失败: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 获取订单列表
     */
    @PostMapping("/order/list")
    public ResponseEntity<Map<String, Object>> getOrderList(@RequestParam Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            Integer page = request.get("page") != null ? Integer.parseInt(request.get("page").toString()) : 1;
            Integer pageSize = request.get("pageSize") != null ? Integer.parseInt(request.get("pageSize").toString()) : 10;
            String status = (String) request.get("status");
            String orderNumber = (String) request.get("orderNumber");

            // 获取订单列表
            List<Order> orderList = orderService.getOrdersByUserId(2L);
            
            // 根据状态过滤订单
            if (status != null && !status.isEmpty()) {
                int statusInt = Integer.parseInt(status);
                orderList = orderList.stream()
                        .filter(order -> order.getStatus() == statusInt)
                        .collect(Collectors.toList());
            }
            
            // 根据订单号搜索
            if (orderNumber != null && !orderNumber.isEmpty()) {
                orderList = orderList.stream()
                        .filter(order -> order.getOrderNumber().contains(orderNumber))
                        .collect(Collectors.toList());
            }
            
            // 模拟商品映射
            Map<String, List<Map<String, Object>>> goodsMap = new HashMap<>();
            
            // 为每个订单创建商品列表
            for (Order order : orderList) {
                List<Map<String, Object>> goodsList = new ArrayList<>();
                
                // 模拟商品数据
                Map<String, Object> good1 = new HashMap<>();
                good1.put("goodsName", "阳山水蜜桃");
                good1.put("pic", "https://via.placeholder.com/100x100?text=Peach");
                good1.put("property", "特级");
                good1.put("amount", 50.0);
                good1.put("number", 2);
                goodsList.add(good1);
                
                // 如果订单金额大于100，添加第二个商品
                if (order.getAmount() > 100) {
                    Map<String, Object> good2 = new HashMap<>();
                    good2.put("goodsName", "黄心油桃");
                    good2.put("pic", "https://via.placeholder.com/100x100?text=Nectarine");
                    good2.put("property", "一级");
                    good2.put("amount", 15.0);
                    good2.put("number", 2);
                    goodsList.add(good2);
                }
                
                goodsMap.put(order.getId().toString(), goodsList);
            }

            // 构建响应数据
            Map<String, Object> data = new HashMap<>();
            data.put("orderList", orderList);
            data.put("goodsMap", goodsMap);

            response.put("code", 0);
            response.put("msg", "success");
            response.put("data", data);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.out.println("=== OrderController: 获取订单列表失败: " + e.getMessage());
            e.printStackTrace();

            response.put("code", 500);
            response.put("msg", "获取订单列表失败: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 生成溯源码
     */
    private String generateTraceCode(String orderNumber) {
        // 简单生成溯源码，实际项目中可能需要更复杂的算法
        return "TRACE_" + orderNumber + "_" + System.currentTimeMillis();
    }

    /**
     * 创建订单
     */
    @PostMapping("/order/create")
    public ResponseEntity<Map<String, Object>> createOrder(
            @RequestParam(required = false) String goodsJsonStr,
            @RequestParam(required = false) String remark,
            @RequestParam(required = false) String linkMan,
            @RequestParam(required = false) String mobile,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String provinceId,
            @RequestParam(required = false) String cityId,
            @RequestParam(required = false) String districtId) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 生成订单号
            String orderNumber = "ORDER" + System.currentTimeMillis();
            
            // 构建订单对象
            Order order = new Order();
            order.setOrderNumber(orderNumber);
            order.setUserId(2L); // 默认用户ID为2
            order.setStatus(0); // 0: 待支付
            order.setStatusStr("待付款");
            
            // 计算金额
            double amount = 0.0;
            try {
                if (goodsJsonStr != null && !goodsJsonStr.isEmpty()) {
                    // 简单模拟计算金额，实际项目中需要解析JSON并计算
                    amount = 90.0; // 模拟商品金额
                }
            } catch (Exception e) {
                System.out.println("=== OrderController: 计算金额失败: " + e.getMessage());
            }
            
            order.setAmount(amount);
            order.setAmountReal(amount);
            order.setAmountLogistics(0.0);
            order.setIsNeedLogistics(1);
            order.setRemark(remark);
            order.setDateAdd(java.time.LocalDateTime.now());
            order.setDateClose(java.time.LocalDateTime.now().plusMinutes(30)); // 30分钟后关闭
            
            // 添加物流信息
            order.setLinkMan(linkMan);
            order.setMobile(mobile);
            order.setAddress(address);
            order.setProvinceId(provinceId);
            order.setCityId(cityId);
            order.setDistrictId(districtId);
            
            // 保存订单
            Order savedOrder = orderService.createOrder(order);
            
            System.out.println("=== OrderController: 订单创建成功，订单号: " + orderNumber + "，金额: " + amount);
            
            // 构建响应数据
            Map<String, Object> data = new HashMap<>();
            data.put("id", savedOrder.getId());
            data.put("orderNumber", orderNumber);
            data.put("amount", amount);
            
            response.put("code", 0);
            response.put("msg", "success");
            response.put("data", data);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.out.println("=== OrderController: 创建订单失败: " + e.getMessage());
            e.printStackTrace();

            response.put("code", 500);
            response.put("msg", "创建订单失败: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 订单支付接口（接收orderId参数）
     */
    @PostMapping("/order/pay")
    public ResponseEntity<Map<String, Object>> payOrder(
            @RequestParam(required = false) String orderNumber,
            @RequestParam(required = false) String orderId) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 获取订单号
            if (orderNumber == null || orderNumber.isEmpty()) {
                // 根据orderId查找订单号
                if (orderId != null && !orderId.isEmpty()) {
                    Long orderIdLong = null;
                    try {
                        orderIdLong = Long.parseLong(orderId);
                    } catch (NumberFormatException e) {
                        System.out.println("=== OrderController: Invalid orderId format: " + orderId);
                    }
                    
                    if (orderIdLong != null) {
                        Order order = orderService.getOrderById(orderIdLong);
                        if (order != null) {
                            orderNumber = order.getOrderNumber();
                        }
                    }
                }
            }
            
            if (orderNumber == null || orderNumber.isEmpty()) {
                response.put("code", 400);
                response.put("msg", "订单编号不能为空");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            // 查找订单
            Order order = orderService.getOrderByOrderNumber(orderNumber);
            if (order == null) {
                response.put("code", 404);
                response.put("msg", "订单不存在");
                response.put("data", null);
                return ResponseEntity.ok(response);
            }

            // 检查订单状态
            int status = order.getStatus();
            if (status != 0) {
                response.put("code", 400);
                response.put("msg", "订单状态错误，只能支付待付款的订单");
                response.put("data", null);
                return ResponseEntity.ok(response);
            }

            // 更新订单状态为已支付
            order.setStatus(1); // 1: 待发货
            order.setStatusStr("待发货");
            order.setPaymentTime(java.time.LocalDateTime.now());
            order.setPaymentMethod("在线支付");

            // 生成溯源码
            String traceCode = generateTraceCode(orderNumber);
            order.setTraceCode(traceCode);

            // 保存更新后的订单
            orderService.updateOrder(order);

            System.out.println("=== OrderController: 订单支付成功，订单号: " + orderNumber + "，溯源码: " + traceCode);

            response.put("code", 0);
            response.put("msg", "支付成功");
            response.put("data", order);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.out.println("=== OrderController: 支付失败: " + e.getMessage());
            e.printStackTrace();

            response.put("code", 500);
            response.put("msg", "支付失败: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(500).body(response);
        }
    }
}
