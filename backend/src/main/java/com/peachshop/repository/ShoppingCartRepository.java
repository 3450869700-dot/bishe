package com.peachshop.repository;

import com.peachshop.model.ShoppingCart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Long> {
    
    /**
     * 使用原生SQL查询所有购物车数据，按创建时间倒序排列（最新的在上层）
     */
    @Query(value = "SELECT * FROM shopping_cart ORDER BY create_time DESC", nativeQuery = true)
    List<Object[]> findAllShoppingCartItems();
    
    /**
     * 根据用户ID获取购物车项目，按创建时间倒序排列（最新的在上层）
     */
    @Query(value = "SELECT * FROM shopping_cart WHERE user_id = ?1 ORDER BY create_time DESC", nativeQuery = true)
    List<Object[]> findByUserId(Long userId);
    
    /**
     * 根据用户ID和商品代码获取购物车项目
     */
    @Query(value = "SELECT * FROM shopping_cart WHERE user_id = ?1 AND product_code = ?2", nativeQuery = true)
    Object[] findByUserIdAndProductCode(Long userId, String productCode);

    /**
     * 根据用户ID和商品代码获取购物车项目（用于检查是否已存在）
     * 注意：由于每个规格是独立的商品记录，使用product_code即可区分不同规格
     */
    @Query(value = "SELECT * FROM shopping_cart WHERE user_id = ?1 AND product_code = ?2", nativeQuery = true)
    Object[] findByUserIdAndProductCodeForCheck(Long userId, Long productCode);
    
    /**
     * 获取购物车项目总数
     */
    @Query(value = "SELECT COUNT(*) FROM shopping_cart", nativeQuery = true)
    Long countShoppingCartItems();
}
