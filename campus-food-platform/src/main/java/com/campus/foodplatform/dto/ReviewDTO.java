/** 店铺评价创建请求 DTO，包含店铺ID、评分、内容和图片字段 */
package com.campus.foodplatform.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class ReviewDTO {
    @NotNull
    private Long shopId;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer rating;

    @NotBlank(message = "评价内容不能为空")
    private String content;

    private String images;
}
