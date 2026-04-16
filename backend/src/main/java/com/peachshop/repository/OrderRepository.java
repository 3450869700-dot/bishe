package com.peachshop.repository;

import com.peachshop.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserIdAndIsValid(Long userId, Integer isValid);
    Order findByOrderNumberAndIsValid(String orderNumber, Integer isValid);
}
