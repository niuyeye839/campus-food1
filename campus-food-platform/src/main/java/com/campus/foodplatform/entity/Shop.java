/** 店铺实体类，对应数据库 shop 表 */
package com.campus.foodplatform.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Shop {
    private Long id;
    private Long merchantId;
    private String name;
    private String category;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String phone;
    private String description;
    private String images;
    private String businessHours;
    /** 加权评分 */
    private BigDecimal score;
    /** 点赞数 */
    private Integer likeCount;
    /** 评价数 */
    private Integer reviewCount;
    /** 是否有学生折扣 */
    private Integer studentDiscount;
    /**
     * 审核状态：0-待审核 1-通过 2-拒绝
     */
    private Integer reviewStatus;
    /** 0-正常 1-下线 */
    private Integer status;
    /**
     * 审核备注（可选）
     */
    private String reviewRemark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
