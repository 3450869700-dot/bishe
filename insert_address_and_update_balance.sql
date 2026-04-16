-- 插入虚拟地址
INSERT INTO shipping_address (user_id, link_man, mobile, province_id, province_str, city_id, city_str, district_id, area_str, address, is_default, is_valid, create_time, update_time)
VALUES (2, '张三', '13800138000', '110000', '北京市', '110100', '北京市', '110101', '东城区', '某某街道某某小区1号楼1单元101室', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 更新用户余额
UPDATE "user" SET balance = 9999 WHERE user_id = 2;