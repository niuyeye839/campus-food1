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
    private String discountType; // percentage-百分比, fixed-固定金额
    private Double discountValue; // 折扣值
    private Double minSpend; // 最低消费
    private Double maxDiscount; // 最高折扣
    private String originalUrl;
    private String source;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    /** 0-有效 1-已过期 */
    private Integer status;
    private LocalDateTime createdAt;
}
