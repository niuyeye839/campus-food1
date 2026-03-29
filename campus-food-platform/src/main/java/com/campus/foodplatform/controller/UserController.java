/** 用户控制器，提供个人信息查询、头像上传和收藏内容接口 */
package com.campus.foodplatform.controller;

import com.campus.foodplatform.common.PageResult;
import com.campus.foodplatform.common.Result;
import com.campus.foodplatform.common.UserContext;
import com.campus.foodplatform.entity.Note;
import com.campus.foodplatform.entity.Shop;
import com.campus.foodplatform.entity.User;
import com.campus.foodplatform.mapper.ShopMapper;
import com.campus.foodplatform.service.FavoriteService;
import com.campus.foodplatform.service.FollowService;
import com.campus.foodplatform.service.NoteService;
import com.campus.foodplatform.service.UserService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final NoteService noteService;
    private final FavoriteService favoriteService;
    private final ShopMapper shopMapper;

    @Value("${app.upload.path}")
    private String uploadPath;

    @GetMapping("/profile")
    public Result<User> profile() {
        return Result.success(userService.getById(UserContext.getUserId()));
    }

    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestParam(required = false) String tastePreference,
            @RequestParam(required = false) Integer budget) {
        userService.updateProfile(UserContext.getUserId(), tastePreference, budget);
        return Result.success();
    }

    @PostMapping("/avatar")
    public Result<String> uploadAvatar(@RequestParam MultipartFile file) throws IOException {
        return Result.success(userService.uploadAvatar(UserContext.getUserId(), file, uploadPath));
    }

    @GetMapping("/notes")
    public Result<PageResult<Note>> myNotes(@RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(noteService.list(null, UserContext.getUserId(), null, null, page, size));
    }

    @GetMapping("/favorites/shops")
    public Result<PageResult<Shop>> favoriteShops(@RequestParam(required = false) Long folderId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(favoriteService.getFavoriteShops(UserContext.getUserId(), folderId, page, size));
    }

    @GetMapping("/favorites/notes")
    public Result<PageResult<Note>> favoriteNotes(@RequestParam(required = false) Long folderId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(favoriteService.getFavoriteNotes(UserContext.getUserId(), folderId, page, size));
    }
}
