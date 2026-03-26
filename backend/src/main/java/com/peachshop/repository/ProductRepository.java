package com.peachshop.repository;

import com.peachshop.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    /**
     * 使用原生SQL查询所有商品数据
     */
    @Query(value = "SELECT * FROM products", nativeQuery = true)
    List<Object[]> findAllProducts();
    
    /**
     * 获取商品总数
     */
    @Query(value = "SELECT COUNT(*) FROM products", nativeQuery = true)
    Long countProducts();
    
    /**
     * 根据商品编码获取商品 - 使用JPA自动查询避免字段映射问题
     */
    Product findByProductCode(Long productCode);
}
