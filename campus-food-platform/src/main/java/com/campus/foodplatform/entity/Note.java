/** 探店笔记实体类，对应数据库 note 表 */
package com.campus.foodplatform.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Note {
    private Long id;
    private Long userId;
    private Long shopId;
    private String title;
    private String content;
    /** IMAGE / VIDEO */
    private String type;
    /** 图片/视频URL，逗号分隔 */
    private String mediaUrls;
    /** 标签，逗号分隔 */
    private String tags;
    private Integer likeCount;
    private Integer commentCount;
    private Integer viewCount;
    /** 0-草稿 1-待审核 2-已发布 3-已拒绝 */
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
