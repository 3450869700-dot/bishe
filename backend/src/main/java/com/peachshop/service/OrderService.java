package com.peachshop.service;

import com.peachshop.model.Order;
import com.peachshop.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    public List<Order> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserIdAndIsValid(userId, 1);
    }
    
    public Order getOrderByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumberAndIsValid(orderNumber, 1);
    }
    
    public Order getOrderById(Long id) {
        return orderRepository.findById(id).orElse(null);
    }
    
    public Order createOrder(Order order) {
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        order.setIsValid(1);
        return orderRepository.save(order);
    }
    
    public Order updateOrder(Order order) {
        order.setUpdateTime(LocalDateTime.now());
        return orderRepository.save(order);
    }
    
    public void deleteOrder(Long id) {
        Order order = orderRepository.findById(id).orElse(null);
        if (order != null) {
            order.setIsValid(0);
            order.setUpdateTime(LocalDateTime.now());
            orderRepository.save(order);
        }
    }
}
