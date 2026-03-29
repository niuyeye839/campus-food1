/** AI 对话请求 DTO，包含操作类型和用户输入内容 */
package com.campus.foodplatform.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class AiChatDTO {
    @NotBlank
    private String action;

    @NotBlank(message = "内容不能为空")
    private String content;
}
