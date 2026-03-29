-- 创建新的用户表（不包含 student_id，使用 phone 作为账号）

USE food_platform;

-- 删除旧表（如果存在）
DROP TABLE IF EXISTS `user`;

-- 创建新的用户表
CREATE TABLE `user` (
    `id`               BIGINT       NOT NULL AUTO_INCREMENT,
    `real_name`        VARCHAR(50)  DEFAULT NULL COMMENT '真实姓名',
    `username`         VARCHAR(50)  NOT NULL COMMENT '用户名',
    `password`         VARCHAR(100) NOT NULL COMMENT '密码',
    `email`            VARCHAR(100) NOT NULL COMMENT '邮箱',
    `phone`            VARCHAR(20)  NOT NULL COMMENT '电话号码（账号）',
    `avatar`           VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `role`             VARCHAR(20)  NOT NULL DEFAULT 'USER' COMMENT '角色：USER/MERCHANT/ADMIN',
    `status`           TINYINT      NOT NULL DEFAULT 0 COMMENT '状态：0正常 1禁用',
    `taste_preference` VARCHAR(255) DEFAULT NULL COMMENT '口味偏好，逗号分隔',
    `budget`           INT          DEFAULT NULL COMMENT '预算（元）',
    `created_at`       DATETIME     NOT NULL COMMENT '创建时间',
    `updated_at`       DATETIME     NOT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_phone` (`phone`),
    UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户表';

-- 插入测试管理员账号
INSERT INTO `user` (`real_name`, `username`, `password`, `email`, `phone`, `role`, `status`, `created_at`, `updated_at`) 
VALUES 
('管理员', 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbdxKnENU5VL2DOYO', 'admin@example.com', '13800138000', 'ADMIN', 0, NOW(), NOW()),
('测试用户', 'testuser', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbdxKnENU5VL2DOYO', 'test@example.com', '13800138001', 'USER', 0, NOW(), NOW());

-- 密码都是: admin123

-- 验证创建结果
SELECT '用户表创建成功！' AS message;
SELECT id, phone, email, real_name, username, role FROM `user`;
