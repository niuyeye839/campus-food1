/** 用户登录请求 DTO，包含账号、密码和角色字段 */
package com.campus.foodplatform.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class LoginDTO {
    @NotBlank(message = "账号不能为空")
    private String account;

    @NotBlank(message = "密码不能为空")
    private String password;

    @Pattern(regexp = "USER|MERCHANT|ADMIN", message = "角色必须是USER、MERCHANT或ADMIN")
    private String role = "USER";
}
