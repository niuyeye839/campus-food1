/** 评论实体类，对应数据库 comment 表 */
package com.campus.foodplatform.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Comment {
    private Long id;
    private Long noteId;
    private Long userId;
    /** 父评论ID，顶级评论为null */
    private Long parentId;
    /** 回复目标用户ID */
    private Long replyUserId;
    private String content;
    private Integer likeCount;
    /** 0-待审核 1-已发布 2-已拒绝 */
    private Integer status;
    private LocalDateTime createdAt;
}
