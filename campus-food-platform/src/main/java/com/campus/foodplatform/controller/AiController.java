/** AI 对话控制器，提供美食推荐、笔记润色等 AI 功能接口 */
package com.campus.foodplatform.controller;

import com.campus.foodplatform.common.Result;
import com.campus.foodplatform.dto.AiChatDTO;
import com.campus.foodplatform.service.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @PostMapping("/chat")
    public Result<String> chat(@Valid @RequestBody AiChatDTO dto) {
        return Result.success(aiService.chat(dto));
    }
}
