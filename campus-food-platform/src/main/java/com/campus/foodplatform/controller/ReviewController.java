/** 评价控制器，提供店铺评价的发布、查询和审核接口 */
package com.campus.foodplatform.controller;

import com.campus.foodplatform.common.PageResult;
import com.campus.foodplatform.common.Result;
import com.campus.foodplatform.common.UserContext;
import com.campus.foodplatform.dto.ReviewDTO;
import com.campus.foodplatform.entity.Review;
import com.campus.foodplatform.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public Result<Review> create(@Valid @RequestBody ReviewDTO dto) {
        return Result.success(reviewService.create(UserContext.getUserId(), dto));
    }

    @GetMapping("/shop/{shopId}")
    public Result<PageResult<Review>> listByShop(@PathVariable Long shopId,
                                                  @RequestParam(defaultValue = "1") int page,
                                                  @RequestParam(defaultValue = "10") int size) {
        return Result.success(reviewService.listByShop(shopId, page, size));
    }

    @GetMapping("/shop/{shopId}/published")
    public Result<PageResult<Review>> listPublishedByShop(@PathVariable Long shopId,
                                                           @RequestParam(defaultValue = "1") int page,
                                                           @RequestParam(defaultValue = "10") int size) {
        return Result.success(reviewService.listPublishedByShop(shopId, page, size));
    }

    @GetMapping("/shop/{shopId}/with-own")
    public Result<PageResult<Review>> listWithOwnReviews(@PathVariable Long shopId,
                                                          @RequestParam(defaultValue = "1") int page,
                                                          @RequestParam(defaultValue = "10") int size) {
        Long userId = UserContext.getUserId();
        return Result.success(reviewService.listWithOwnReviews(shopId, userId, page, size));
    }

    @GetMapping("/pending")
    public Result<PageResult<Review>> pending(@RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "10") int size) {
        return Result.success(reviewService.pendingList(page, size));
    }

    @PutMapping("/{id}/review")
    public Result<Void> adminReview(@PathVariable Long id, @RequestParam Integer status) {
        reviewService.adminReview(id, status);
        return Result.success();
    }
    
    /**
     * 商家回复评价
     */
    @PostMapping("/{id}/reply")
    public Result<Void> merchantReply(@PathVariable Long id, @RequestParam String content) {
        reviewService.merchantReply(id, UserContext.getUserId(), content);
        return Result.success();
    }
}
