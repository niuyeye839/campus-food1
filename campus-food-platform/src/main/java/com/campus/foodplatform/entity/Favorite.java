/** 收藏实体类，对应数据库 favorite 表，支持店铺和笔记收藏 */
package com.campus.foodplatform.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Favorite {
    private Long id;
    private Long userId;
    /** SHOP / NOTE */
    private String targetType;
    private Long targetId;
    private LocalDateTime createdAt;
}
