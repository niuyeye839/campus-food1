/** 优惠信息实体类，对应数据库 discount 表 */
package com.campus.foodplatform.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Discount {
    private Long id;
    private Long shopId;
    private String title;
    private String description;
    private String originalUrl;
    private String source;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    /** 0-有效 1-已过期 */
    private Integer status;
    private LocalDateTime createdAt;
}
