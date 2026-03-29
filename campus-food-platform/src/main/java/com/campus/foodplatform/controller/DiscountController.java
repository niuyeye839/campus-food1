/** 优惠信息控制器，提供校园周边优惠活动列表查询接口 */
package com.campus.foodplatform.controller;

import com.campus.foodplatform.common.PageResult;
import com.campus.foodplatform.common.Result;
import com.campus.foodplatform.entity.Discount;
import com.campus.foodplatform.service.DiscountService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/discounts")
@RequiredArgsConstructor
public class DiscountController {

    private final DiscountService discountService;

    @GetMapping("/list")
    public Result<PageResult<Discount>> list(@RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "10") int size) {
        return Result.success(discountService.list(page, size));
    }
}
