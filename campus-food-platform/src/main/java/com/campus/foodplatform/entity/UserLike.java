/** 用户点赞实体类，对应数据库 user_like 表，支持笔记、评价和评论点赞 */
package com.campus.foodplatform.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserLike {
    private Long id;
    private Long userId;
    /** NOTE / REVIEW / COMMENT */
    private String targetType;
    private Long targetId;
    private LocalDateTime createdAt;
}
