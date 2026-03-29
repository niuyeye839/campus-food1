/** 排行榜控制器，提供店铺总榜、月榜和分类榜查询接口 */
package com.campus.foodplatform.controller;

import com.campus.foodplatform.common.Result;
import com.campus.foodplatform.service.RankService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rank")
@RequiredArgsConstructor
public class RankController {

    private final RankService rankService;

    @GetMapping("/total")
    public Result<List<Map<String, Object>>> total(@RequestParam(defaultValue = "20") int size) {
        return Result.success(rankService.totalRank(size));
    }

    @GetMapping("/monthly")
    public Result<List<Map<String, Object>>> monthly(@RequestParam(defaultValue = "20") int size) {
        return Result.success(rankService.monthlyRank(size));
    }

    @GetMapping("/category")
    public Result<List<Map<String, Object>>> category(@RequestParam String category,
                                                       @RequestParam(defaultValue = "20") int size) {
        return Result.success(rankService.categoryRank(category, size));
    }
}
