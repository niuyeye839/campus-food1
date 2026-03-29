/** 探店笔记控制器，提供笔记发布、编辑、查询和审核接口 */
package com.campus.foodplatform.controller;

import com.campus.foodplatform.common.PageResult;
import com.campus.foodplatform.common.Result;
import com.campus.foodplatform.common.UserContext;
import com.campus.foodplatform.dto.NoteDTO;
import com.campus.foodplatform.entity.Note;
import com.campus.foodplatform.service.LikeService;
import com.campus.foodplatform.service.NoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;
    private final LikeService likeService;

    @GetMapping("/list")
    public Result<PageResult<Note>> list(@RequestParam(required = false) Long shopId,
                                         @RequestParam(required = false) String keyword,
                                         @RequestParam(defaultValue = "1") int page,
                                         @RequestParam(defaultValue = "10") int size) {
        return Result.success(noteService.list(shopId, null, 2, keyword, page, size));
    }

    @GetMapping("/{id}")
    public Result<Note> detail(@PathVariable Long id) {
        return Result.success(noteService.getById(id));
    }

    @PostMapping
    public Result<Note> publish(@Valid @RequestBody NoteDTO dto) {
        return Result.success(noteService.publish(UserContext.getUserId(), dto));
    }

    @PostMapping("/draft")
    public Result<Void> saveDraft(@RequestBody NoteDTO dto) {
        noteService.saveDraft(UserContext.getUserId(), dto);
        return Result.success();
    }

    @GetMapping("/draft")
    public Result<NoteDTO> getDraft() {
        return Result.success(noteService.getDraft(UserContext.getUserId()));
    }

    @PutMapping("/{id}")
    public Result<Note> update(@PathVariable Long id, @Valid @RequestBody NoteDTO dto) {
        return Result.success(noteService.update(UserContext.getUserId(), id, dto));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        noteService.delete(UserContext.getUserId(), id);
        return Result.success();
    }

    /** 查询当前登录用户是否已点赞该笔记 */
    @GetMapping("/{id}/liked")
    public Result<Boolean> isLiked(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        return Result.success(likeService.isLiked(userId, "NOTE", id));
    }

    @PutMapping("/{id}/review")
    public Result<Void> adminReview(@PathVariable Long id, @RequestParam Integer status) {
        noteService.adminReview(id, status);
        return Result.success();
    }
}
