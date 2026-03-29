/** 关注实体类，对应数据库 follow 表，支持关注店铺和用户 */
package com.campus.foodplatform.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Follow {
    private Long id;
    private Long userId;
    /** SHOP / USER */
    private String targetType;
    private Long targetId;
    private LocalDateTime createdAt;
}
