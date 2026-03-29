-- 创建收藏夹表

USE food_platform;

-- 收藏夹表
CREATE TABLE IF NOT EXISTS `favorite_folder` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`    BIGINT       NOT NULL COMMENT '用户ID',
    `name`       VARCHAR(50)  NOT NULL COMMENT '收藏夹名称',
    `is_default` TINYINT      NOT NULL DEFAULT 0 COMMENT '是否默认收藏夹',
    `created_at` DATETIME     NOT NULL,
    `updated_at` DATETIME     NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='收藏夹表';

-- 修改 favorite 表，添加 folder_id 字段
ALTER TABLE `favorite` 
ADD COLUMN `folder_id` BIGINT DEFAULT NULL COMMENT '收藏夹ID' AFTER `user_id`,
ADD KEY `idx_folder_id` (`folder_id`);

-- 为现有用户创建默认收藏夹
INSERT INTO `favorite_folder` (`user_id`, `name`, `is_default`, `created_at`, `updated_at`)
SELECT id, '默认收藏夹', 1, NOW(), NOW() FROM `user`;

-- 将现有收藏关联到默认收藏夹
UPDATE `favorite` f
INNER JOIN `favorite_folder` ff ON f.user_id = ff.user_id AND ff.is_default = 1
SET f.folder_id = ff.id
WHERE f.folder_id IS NULL;

SELECT '收藏夹表创建完成' AS message;
