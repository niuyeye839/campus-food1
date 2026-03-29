/** 消息控制器，提供消息列表查询、未读数统计和标记已读接口 */
package com.campus.foodplatform.controller;

import com.campus.foodplatform.common.PageResult;
import com.campus.foodplatform.common.Result;
import com.campus.foodplatform.common.UserContext;
import com.campus.foodplatform.entity.Message;
import com.campus.foodplatform.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @GetMapping
    public Result<PageResult<Message>> list(@RequestParam String type,
                                             @RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "20") int size) {
        return Result.success(messageService.list(UserContext.getUserId(), type, page, size));
    }

    @GetMapping("/unread-count")
    public Result<Integer> unreadCount() {
        return Result.success(messageService.unreadCount(UserContext.getUserId()));
    }

    @PutMapping("/read")
    public Result<Void> markRead(@RequestParam String type) {
        messageService.markRead(UserContext.getUserId(), type);
        return Result.success();
    }
}
