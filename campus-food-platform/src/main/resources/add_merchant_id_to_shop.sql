-- 为 shop 表添加 merchant_id 字段，关联商家用户
-- 执行此脚本前请备份数据库

USE food_platform;

-- 添加 merchant_id 字段
ALTER TABLE `shop` 
ADD COLUMN `merchant_id` BIGINT DEFAULT NULL COMMENT '商家用户ID' AFTER `id`,
ADD KEY `idx_merchant_id` (`merchant_id`);

-- 如果需要，可以为现有店铺设置 merchant_id
-- UPDATE `shop` SET `merchant_id` = ? WHERE `id` = ?;
