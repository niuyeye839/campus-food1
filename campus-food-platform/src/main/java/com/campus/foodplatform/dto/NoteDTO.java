/** 探店笔记创建/编辑请求 DTO，包含标题、内容、媒体和标签等字段 */
package com.campus.foodplatform.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class NoteDTO {
    @NotBlank(message = "标题不能为空")
    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;

    @NotNull(message = "店铺ID不能为空")
    private Long shopId;

    @NotBlank(message = "笔记类型不能为空")
    private String type;

    private String mediaUrls;
    private String tags;

    private boolean publish = true;
}
