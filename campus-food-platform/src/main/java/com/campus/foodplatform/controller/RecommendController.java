/** 推荐控制器，提供基于用户偏好的个性化店铺推荐接口 */
package com.campus.foodplatform.controller;

import com.campus.foodplatform.common.Result;
import com.campus.foodplatform.common.UserContext;
import com.campus.foodplatform.entity.Shop;
import com.campus.foodplatform.service.RecommendService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommend")
@RequiredArgsConstructor
public class RecommendController {

    private final RecommendService recommendService;

    @GetMapping("/shops")
    public Result<List<Shop>> recommendShops(@RequestParam(defaultValue = "10") int size) {
        return Result.success(recommendService.recommend(UserContext.getUserId(), size));
    }
}
