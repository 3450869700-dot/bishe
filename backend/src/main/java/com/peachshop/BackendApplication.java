package com.peachshop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.Map;

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
            
            // 先删除shipping_address表，然后重新创建
            try {
                String dropAddressTableQuery = "DROP TABLE IF EXISTS shipping_address";
                jdbcTemplate.execute(dropAddressTableQuery);
                System.out.println("=== BackendApplication: Successfully dropped shipping_address table");
            } catch (Exception e) {
                System.out.println("=== BackendApplication: Error dropping shipping_address table: " + e.getMessage());
            }
            
            // 创建shipping_address表
            String createAddressTableQuery = "CREATE TABLE shipping_address (" +
                                           "id BIGSERIAL PRIMARY KEY, " +
                                           "user_id BIGINT NOT NULL, " +
                                           "link_man VARCHAR(50) NOT NULL, " +
                                           "mobile VARCHAR(11) NOT NULL, " +
                                           "province_id VARCHAR(10), " +
                                           "province_str VARCHAR(50), " +
                                           "city_id VARCHAR(10), " +
                                           "city_str VARCHAR(50), " +
                                           "district_id VARCHAR(10), " +
                                           "area_str VARCHAR(50), " +
                                           "address VARCHAR(200) NOT NULL, " +
                                           "is_default SMALLINT NOT NULL DEFAULT 0, " +
                                           "is_valid SMALLINT NOT NULL DEFAULT 1, " +
                                           "create_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                                           "update_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP" +
                                           ")";
            jdbcTemplate.execute(createAddressTableQuery);
            System.out.println("=== BackendApplication: Successfully created shipping_address table");
            
            // 注意：数据库表中没有maturity字段，规格信息通过product_code区分
            // 每个规格是独立的商品记录，有自己的product_code
            System.out.println("=== BackendApplication: Note - shopping_cart table does not have maturity column, using product_code to distinguish specifications");
            
            // 检查user表是否存在balance字段
            try {
                String checkBalanceColumnQuery = "SELECT COUNT(*) FROM information_schema.columns " +
                                               "WHERE table_name = 'user' AND column_name = 'balance'";
                Integer balanceColumnCount = jdbcTemplate.queryForObject(checkBalanceColumnQuery, Integer.class);
                
                if (balanceColumnCount == null || balanceColumnCount == 0) {
                    // 如果字段不存在，添加balance字段
                    String addBalanceColumnQuery = "ALTER TABLE \"user\" ADD COLUMN balance double precision NOT NULL DEFAULT 0.0";
                    jdbcTemplate.execute(addBalanceColumnQuery);
                    System.out.println("=== BackendApplication: Successfully added balance column to user table");
                } else {
                    System.out.println("=== BackendApplication: user table already has balance column");
                }
            } catch (Exception e) {
                System.out.println("=== BackendApplication: Error checking/fixing balance column: " + e.getMessage());
            }
            
            // 插入虚拟地址
            try {
                String insertAddressQuery = "INSERT INTO shipping_address (user_id, link_man, mobile, province_id, province_str, city_id, city_str, district_id, area_str, address, is_default, is_valid, create_time, update_time) " +
                                         "VALUES (2, '张三', '13800138000', '110000', '北京市', '110100', '北京市', '110101', '东城区', '某某街道某某小区1号楼1单元101室', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
                jdbcTemplate.execute(insertAddressQuery);
                System.out.println("=== BackendApplication: Successfully inserted virtual address");
            } catch (Exception e) {
                System.out.println("=== BackendApplication: Error inserting virtual address: " + e.getMessage());
            }
            
            // 设置用户余额为9999
            try {
                String updateBalanceQuery = "UPDATE \"user\" SET balance = 9999 WHERE user_id = 2";
                jdbcTemplate.execute(updateBalanceQuery);
                System.out.println("=== BackendApplication: Successfully updated user balance to 9999");
            } catch (Exception e) {
                System.out.println("=== BackendApplication: Error updating user balance: " + e.getMessage());
            }

            // 创建orders表（如果不存在）
            try {
                String createOrdersTableQuery = "CREATE TABLE IF NOT EXISTS orders (" +
                                            "id BIGSERIAL PRIMARY KEY, " +
                                            "order_number VARCHAR(50) NOT NULL, " +
                                            "user_id BIGINT NOT NULL, " +
                                            "status INTEGER NOT NULL DEFAULT 0, " +
                                            "status_str VARCHAR(20), " +
                                            "amount DOUBLE PRECISION NOT NULL DEFAULT 0, " +
                                            "amount_real DOUBLE PRECISION NOT NULL DEFAULT 0, " +
                                            "amount_logistics DOUBLE PRECISION NOT NULL DEFAULT 0, " +
                                            "is_need_logistics SMALLINT NOT NULL DEFAULT 1, " +
                                            "remark VARCHAR(500), " +
                                            "date_add TIMESTAMP WITH TIME ZONE, " +
                                            "date_close TIMESTAMP WITH TIME ZONE, " +
                                            "payment_time TIMESTAMP WITH TIME ZONE, " +
                                            "payment_method VARCHAR(20), " +
                                            "trace_code VARCHAR(100), " +
                                            "link_man VARCHAR(50), " +
                                            "mobile VARCHAR(11), " +
                                            "address VARCHAR(200), " +
                                            "province_id VARCHAR(10), " +
                                            "city_id VARCHAR(10), " +
                                            "district_id VARCHAR(10), " +
                                            "create_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                                            "update_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                                            "is_valid SMALLINT NOT NULL DEFAULT 1" +
                                            ")";
                jdbcTemplate.execute(createOrdersTableQuery);
                System.out.println("=== BackendApplication: Successfully created orders table");
            } catch (Exception e) {
                System.out.println("=== BackendApplication: Error creating orders table: " + e.getMessage());
            }
        } catch (Exception e) {
            System.out.println("=== BackendApplication: Error checking/fixing database schema: " + e.getMessage());
            e.printStackTrace();
        }
    }

}