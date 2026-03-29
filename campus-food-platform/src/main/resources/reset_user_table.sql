-- 重置用户表脚本（清空数据并重建表结构）
-- 警告：此脚本会删除所有用户数据！仅用于开发环境！

USE food_platform;

-- 删除旧的用户表
DROP TABLE IF EXISTS `user`;

-- 创建新的用户表（不包含 student_id）
CREATE TABLE `user` (
    `id`               BIGINT       NOT NULL AUTO_INCREMENT,
    `real_name`        VARCHAR(50)  DEFAULT NULL COMMENT '真实姓名',
    `username`         VARCHAR(50)  NOT NULL,
    `password`         VARCHAR(100) NOT NULL,
    `email`            VARCHAR(100) NOT NULL COMMENT '邮箱',
    `phone`            VARCHAR(20)  NOT NULL COMMENT '电话号码（账号）',
    `avatar`           VARCHAR(255) DEFAULT NULL,
    `role`             VARCHAR(20)  NOT NULL DEFAULT 'USER' COMMENT 'USER/MERCHANT/ADMIN',
    `status`           TINYINT      NOT NULL DEFAULT 0 COMMENT '0正常 1禁用',
    `taste_preference` VARCHAR(255) DEFAULT NULL COMMENT '口味偏好，逗号分隔',
    `budget`           INT          DEFAULT NULL COMMENT '预算（元）',
    `created_at`       DATETIME     NOT NULL,
    `updated_at`       DATETIME     NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_phone` (`phone`),
    UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 插入测试管理员账号（可选）
INSERT INTO `user` (`real_name`, `username`, `password`, `email`, `phone`, `role`, `status`, `created_at`, `updated_at`) 
VALUES ('管理员', 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbdxKnENU5VL2DOYO', 'admin@example.com', '13800138000', 'ADMIN', 0, NOW(), NOW());
-- 密码是: admin123

SELECT '用户表已重置，student_id 已移除，phone 作为账号' AS message;
