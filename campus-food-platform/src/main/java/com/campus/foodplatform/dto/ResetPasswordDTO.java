/** 重置密码请求 DTO，包含邮箱、验证码和新密码字段 */
package com.campus.foodplatform.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class ResetPasswordDTO {
    @NotBlank
    @Email
    private String email;

    @NotBlank(message = "验证码不能为空")
    private String code;

    @NotBlank
    @Size(min = 6, max = 20, message = "密码长度6-20位")
    private String newPassword;
}
