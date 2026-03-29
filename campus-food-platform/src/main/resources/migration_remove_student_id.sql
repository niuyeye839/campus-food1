-- 数据库迁移脚本：移除 student_id，使用 phone 作为账号
-- 执行前请备份数据库！

USE food_platform;

-- 步骤1：为现有用户填充默认的 phone 和 email（如果为空）
UPDATE `user` 
SET 
    `phone` = CONCAT('138', LPAD(id, 8, '0')),
    `email` = CONCAT('user', id, '@example.com')
WHERE `phone` IS NULL OR `phone` = '' OR `email` IS NULL OR `email` = '';

-- 步骤2：修改 phone 和 email 字段为 NOT NULL
ALTER TABLE `user` 
    MODIFY COLUMN `phone` VARCHAR(20) NOT NULL COMMENT '电话号码（账号）',
    MODIFY COLUMN `email` VARCHAR(100) NOT NULL COMMENT '邮箱';

-- 步骤3：添加 phone 的唯一索引（如果不存在）
ALTER TABLE `user` ADD UNIQUE KEY `uk_phone` (`phone`);

-- 步骤4：删除 student_id 字段（会自动删除相关索引）
ALTER TABLE `user` DROP COLUMN `student_id`;

-- 验证修改
SHOW CREATE TABLE `user`;
SELECT id, phone, email, real_name, username, role FROM `user` LIMIT 5;
