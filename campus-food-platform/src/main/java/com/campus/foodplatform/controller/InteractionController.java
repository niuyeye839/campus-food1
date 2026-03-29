/** 互动控制器，提供点赞、关注、收藏的切换接口 */
package com.campus.foodplatform.controller;

import com.campus.foodplatform.common.Result;
import com.campus.foodplatform.common.UserContext;
import com.campus.foodplatform.service.FavoriteService;
import com.campus.foodplatform.service.FollowService;
import com.campus.foodplatform.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/interact")
@RequiredArgsConstructor
public class InteractionController {

    private final LikeService likeService;
    private final FollowService followService;
    private final FavoriteService favoriteService;

    /** 点赞/取消点赞 targetType: NOTE/REVIEW/COMMENT */
    @PostMapping("/like/{targetType}/{targetId}")
    public Result<Boolean> like(@PathVariable String targetType, @PathVariable Long targetId) {
        boolean liked = likeService.toggle(UserContext.getUserId(), targetType.toUpperCase(), targetId);
        return Result.success(liked);
    }

    /** 关注/取消关注 targetType: SHOP/USER */
    @PostMapping("/follow/{targetType}/{targetId}")
    public Result<Boolean> follow(@PathVariable String targetType, @PathVariable Long targetId) {
        boolean followed = followService.toggle(UserContext.getUserId(), targetType.toUpperCase(), targetId);
        return Result.success(followed);
    }

    /** 收藏/取消收藏 targetType: SHOP/NOTE */
    @PostMapping("/favorite/{targetType}/{targetId}")
    public Result<Boolean> favorite(@PathVariable String targetType, @PathVariable Long targetId) {
        boolean favorited = favoriteService.toggle(UserContext.getUserId(), targetType.toUpperCase(), targetId);
        return Result.success(favorited);
    }
}
