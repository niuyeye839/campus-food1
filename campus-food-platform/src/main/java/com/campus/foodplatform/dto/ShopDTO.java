/** 店铺创建/编辑请求 DTO，包含名称、分类、地址等基本信息 */
package com.campus.foodplatform.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;

@Data
public class ShopDTO {
    @NotBlank(message = "店铺名称不能为空")
    private String name;

    @NotBlank(message = "分类不能为空")
    private String category;

    @NotBlank(message = "地址不能为空")
    private String address;

    private BigDecimal latitude;
    private BigDecimal longitude;
    private String phone;
    private String description;
    private String images;
    private String businessHours;
    private Integer studentDiscount;
}
