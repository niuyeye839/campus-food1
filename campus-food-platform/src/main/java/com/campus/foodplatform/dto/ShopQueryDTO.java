/** 店铺列表查询条件 DTO，支持关键词、分类、评分和排序等筛选 */
package com.campus.foodplatform.dto;

import lombok.Data;

@Data
public class ShopQueryDTO {
    private String keyword;
    private String category;
    /** 最低评分 */
    private Double minScore;
    /** 排序：score/hot/distance */
    private String sortBy;
    /** 是否有学生折扣 */
    private Integer studentDiscount;
    private Integer page = 1;
    private Integer size = 10;
    /** 用户经度（距离排序用） */
    private Double longitude;
    /** 用户纬度 */
    private Double latitude;
}
