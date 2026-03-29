/** 评论创建请求 DTO，包含笔记ID、内容和回复目标信息 */
package com.campus.foodplatform.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class CommentDTO {
    @NotNull
    private Long noteId;

    @NotBlank(message = "评论内容不能为空")
    private String content;

    private Long parentId;
    private Long replyUserId;
}
