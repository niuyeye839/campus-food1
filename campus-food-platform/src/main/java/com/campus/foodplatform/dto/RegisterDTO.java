/** 用户注册请求 DTO，包含学号/统一社会信用代码、姓名、密码和角色字段 */
package com.campus.foodplatform.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
public class RegisterDTO {
    @NotBlank(message = "账号不能为空")
    private String studentId;

    @NotBlank(message = "姓名不能为空")
    @Size(min = 2, max = 20, message = "姓名长度2-20位")
    private String realName;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度6-20位")
    private String password;

    @Pattern(regexp = "USER|MERCHANT", message = "角色必须是USER或MERCHANT")
    private String role = "USER";
}
