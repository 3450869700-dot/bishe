package com.peachshop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.peachshop.repository")
@EntityScan(basePackages = "com.peachshop.model")
public class BackendApplication {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        System.out.println("=== BackendApplication: Checking and fixing database schema...");
        try {
            // 检查shopping_cart表是否存在is_checked字段
            String checkColumnQuery = "SELECT COUNT(*) FROM information_schema.columns " +
                                     "WHERE table_name = 'shopping_cart' AND column_name = 'is_checked'";
            Integer count = jdbcTemplate.queryForObject(checkColumnQuery, Integer.class);
            
            if (count == null || count == 0) {
                // 如果字段不存在，添加is_checked字段
                String addColumnQuery = "ALTER TABLE shopping_cart ADD COLUMN is_checked SMALLINT NOT NULL DEFAULT 1";
                jdbcTemplate.execute(addColumnQuery);
                System.out.println("=== BackendApplication: Successfully added is_checked column to shopping_cart table");
            } else {
                System.out.println("=== BackendApplication: shopping_cart table already has is_checked column");
            }
            
            // 注意：数据库表中没有maturity字段，规格信息通过product_code区分
            // 每个规格是独立的商品记录，有自己的product_code
            System.out.println("=== BackendApplication: Note - shopping_cart table does not have maturity column, using product_code to distinguish specifications");
        } catch (Exception e) {
            System.out.println("=== BackendApplication: Error checking/fixing database schema: " + e.getMessage());
            e.printStackTrace();
        }
    }

}