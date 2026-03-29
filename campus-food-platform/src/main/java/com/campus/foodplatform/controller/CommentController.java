/** 评论控制器，提供笔记评论的发布、查询和删除接口 */
package com.campus.foodplatform.controller;

import com.campus.foodplatform.common.PageResult;
import com.campus.foodplatform.common.Result;
import com.campus.foodplatform.common.UserContext;
import com.campus.foodplatform.dto.CommentDTO;
import com.campus.foodplatform.entity.Comment;
import com.campus.foodplatform.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public Result<Comment> create(@Valid @RequestBody CommentDTO dto) {
        return Result.success(commentService.create(UserContext.getUserId(), dto));
    }

    @GetMapping("/note/{noteId}")
    public Result<PageResult<Comment>> listByNote(@PathVariable Long noteId,
                                                   @RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "10") int size) {
        return Result.success(commentService.listByNote(noteId, page, size));
    }

    @GetMapping("/{parentId}/replies")
    public Result<List<Comment>> replies(@PathVariable Long parentId) {
        return Result.success(commentService.getReplies(parentId));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        commentService.delete(UserContext.getUserId(), id);
        return Result.success();
    }
}
