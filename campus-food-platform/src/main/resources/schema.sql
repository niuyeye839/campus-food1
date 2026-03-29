-- 校园美食平台数据库建表脚本
-- 适配 MySQL 5.5

CREATE DATABASE IF NOT EXISTS food_platform DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;
USE food_platform;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id`               BIGINT       NOT NULL AUTO_INCREMENT,
    `student_id`       VARCHAR(50)  DEFAULT NULL COMMENT '学号',
    `real_name`        VARCHAR(50)  DEFAULT NULL COMMENT '真实姓名',
    `username`         VARCHAR(50)  NOT NULL,
    `password`         VARCHAR(100) NOT NULL,
    `email`            VARCHAR(100) DEFAULT NULL,
    `phone`            VARCHAR(20)  DEFAULT NULL,
    `avatar`           VARCHAR(255) DEFAULT NULL,
    `role`             VARCHAR(20)  NOT NULL DEFAULT 'USER' COMMENT 'USER/MERCHANT/ADMIN',
    `status`           TINYINT      NOT NULL DEFAULT 0 COMMENT '0正常 1禁用',
    `taste_preference` VARCHAR(255) DEFAULT NULL COMMENT '口味偏好，逗号分隔',
    `budget`           INT          DEFAULT NULL COMMENT '预算（元）',
    `created_at`       DATETIME     NOT NULL,
    `updated_at`       DATETIME     NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_student_id` (`student_id`),
    UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 店铺表
CREATE TABLE IF NOT EXISTS `shop` (
    `id`               BIGINT        NOT NULL AUTO_INCREMENT,
    `name`             VARCHAR(100)  NOT NULL,
    `category`         VARCHAR(50)   NOT NULL,
    `address`          VARCHAR(255)  NOT NULL,
    `latitude`         DECIMAL(10,7) DEFAULT NULL,
    `longitude`        DECIMAL(10,7) DEFAULT NULL,
    `phone`            VARCHAR(20)   DEFAULT NULL,
    `description`      TEXT,
    `images`           TEXT          COMMENT '图片URL逗号分隔',
    `business_hours`   VARCHAR(100)  DEFAULT NULL,
    `score`            DECIMAL(3,2)  NOT NULL DEFAULT 0.00 COMMENT '加权评分',
    `like_count`       INT           NOT NULL DEFAULT 0,
    `review_count`     INT           NOT NULL DEFAULT 0,
    `student_discount` TINYINT       NOT NULL DEFAULT 0 COMMENT '1有学生折扣',
    `status`           TINYINT       NOT NULL DEFAULT 0 COMMENT '0正常 1下线',
    `created_at`       DATETIME      NOT NULL,
    `updated_at`       DATETIME      NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_category` (`category`),
    KEY `idx_score` (`score`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 探店笔记表
CREATE TABLE IF NOT EXISTS `note` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`       BIGINT       NOT NULL,
    `shop_id`       BIGINT       NOT NULL,
    `title`         VARCHAR(200) NOT NULL,
    `content`       TEXT         NOT NULL,
    `type`          VARCHAR(10)  NOT NULL COMMENT 'IMAGE/VIDEO',
    `media_urls`    TEXT         COMMENT '媒体URL逗号分隔',
    `tags`          VARCHAR(255) DEFAULT NULL COMMENT '标签逗号分隔',
    `like_count`    INT          NOT NULL DEFAULT 0,
    `comment_count` INT          NOT NULL DEFAULT 0,
    `view_count`    INT          NOT NULL DEFAULT 0,
    `status`        TINYINT      NOT NULL DEFAULT 0 COMMENT '0草稿 1待审核 2已发布 3已拒绝',
    `created_at`    DATETIME     NOT NULL,
    `updated_at`    DATETIME     NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_shop_id` (`shop_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 评价表
CREATE TABLE IF NOT EXISTS `review` (
    `id`         BIGINT   NOT NULL AUTO_INCREMENT,
    `user_id`    BIGINT   NOT NULL,
    `shop_id`    BIGINT   NOT NULL,
    `rating`     TINYINT  NOT NULL COMMENT '1-5分',
    `content`    TEXT     NOT NULL,
    `images`     TEXT,
    `like_count` INT      NOT NULL DEFAULT 0,
    `status`     TINYINT  NOT NULL DEFAULT 0 COMMENT '0待审核 1已发布 2已拒绝',
    `created_at` DATETIME NOT NULL,
    `updated_at` DATETIME NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_shop_id` (`shop_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 评论表
CREATE TABLE IF NOT EXISTS `comment` (
    `id`            BIGINT   NOT NULL AUTO_INCREMENT,
    `note_id`       BIGINT   NOT NULL,
    `user_id`       BIGINT   NOT NULL,
    `parent_id`     BIGINT   DEFAULT NULL COMMENT '父评论ID，顶级为NULL',
    `reply_user_id` BIGINT   DEFAULT NULL COMMENT '回复目标用户ID',
    `content`       TEXT     NOT NULL,
    `like_count`    INT      NOT NULL DEFAULT 0,
    `status`        TINYINT  NOT NULL DEFAULT 1 COMMENT '0待审核 1已发布 2已拒绝',
    `created_at`    DATETIME NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_note_id` (`note_id`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 优惠信息表
CREATE TABLE IF NOT EXISTS `discount` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT,
    `shop_id`      BIGINT       DEFAULT NULL,
    `title`        VARCHAR(200) NOT NULL,
    `description`  TEXT,
    `original_url` VARCHAR(500) DEFAULT NULL,
    `source`       VARCHAR(100) DEFAULT NULL,
    `start_time`   DATETIME     DEFAULT NULL,
    `end_time`     DATETIME     DEFAULT NULL,
    `status`       TINYINT      NOT NULL DEFAULT 0 COMMENT '0有效 1已过期',
    `created_at`   DATETIME     NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_shop_id` (`shop_id`),
    KEY `idx_end_time` (`end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 关注表
CREATE TABLE IF NOT EXISTS `follow` (
    `id`          BIGINT      NOT NULL AUTO_INCREMENT,
    `user_id`     BIGINT      NOT NULL,
    `target_type` VARCHAR(10) NOT NULL COMMENT 'SHOP/USER',
    `target_id`   BIGINT      NOT NULL,
    `created_at`  DATETIME    NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_follow` (`user_id`, `target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 收藏表
CREATE TABLE IF NOT EXISTS `favorite` (
    `id`          BIGINT      NOT NULL AUTO_INCREMENT,
    `user_id`     BIGINT      NOT NULL,
    `target_type` VARCHAR(10) NOT NULL COMMENT 'SHOP/NOTE',
    `target_id`   BIGINT      NOT NULL,
    `created_at`  DATETIME    NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_favorite` (`user_id`, `target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 点赞表
CREATE TABLE IF NOT EXISTS `user_like` (
    `id`          BIGINT      NOT NULL AUTO_INCREMENT,
    `user_id`     BIGINT      NOT NULL,
    `target_type` VARCHAR(10) NOT NULL COMMENT 'NOTE/REVIEW/COMMENT',
    `target_id`   BIGINT      NOT NULL,
    `created_at`  DATETIME    NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_like` (`user_id`, `target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 消息表
CREATE TABLE IF NOT EXISTS `message` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`    BIGINT       NOT NULL,
    `type`       VARCHAR(20)  NOT NULL COMMENT 'INTERACTION/DISCOUNT/SYSTEM',
    `title`      VARCHAR(100) NOT NULL,
    `content`    TEXT         NOT NULL,
    `related_id` BIGINT       DEFAULT NULL,
    `is_read`    TINYINT      NOT NULL DEFAULT 0,
    `created_at` DATETIME     NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
