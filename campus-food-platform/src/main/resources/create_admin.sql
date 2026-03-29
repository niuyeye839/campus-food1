-- 创建管理员账号
USE food_platform;

-- 删除可能存在的测试账号
DELETE FROM `user` WHERE phone IN ('13800138000', '13800138001');

-- 插入管理员账号
-- 用户名: admin  密码: admin123  电话: 13800138000
INSERT INTO `user` (`real_name`, `username`, `password`, `email`, `phone`, `role`, `status`, `created_at`, `updated_at`) 
VALUES ('管理员', 'admin', '$2a$10$5Z8L0SyuW8L0SyuW8L0SyuW8L0SyuW8L0SyuW8L0SyuW8L0SyuW8L0Sy', 'admin@campus.com', '13800138000', 'ADMIN', 0, NOW(), NOW());

-- 插入测试用户
-- 用户名: testuser  密码: 123456  电话: 13800138001
INSERT INTO `user` (`real_name`, `username`, `password`, `email`, `phone`, `role`, `status`, `created_at`, `updated_at`) 
VALUES ('测试用户', 'testuser', '$2a$10$5Z8L0SyuW8L0SyuW8L0SyuW8L0SyuW8L0SyuW8L0SyuW8L0SyuW8L0Sy', 'test@campus.com', '13800138001', 'USER', 0, NOW(), NOW());

SELECT '账号创建完成' AS message;
SELECT id, phone, email, username, role FROM `user`;
