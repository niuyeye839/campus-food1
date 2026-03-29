/** 店铺评价实体类，对应数据库 review 表 */
package com.campus.foodplatform.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Review {
    private Long id;
    private Long userId;
    private Long shopId;
    private Integer rating;
    private String content;
    private String images;
    private Integer likeCount;
    /** 0-待审核 1-已发布 2-已拒绝 */
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /** 商家回复内容 */
    private String merchantReply;
    /** 商家回复时间 */
    private LocalDateTime merchantReplyTime;
    
    /** 用户名（关联查询） */
    private String username;
    /** 真实姓名（关联查询） */
    private String realName;
    /** 用户头像（关联查询） */
    private String avatar;
}
