/** 店铺控制器，提供店铺的增删改查、热门排行和状态管理接口 */
package com.campus.foodplatform.controller;

import com.campus.foodplatform.common.PageResult;
import com.campus.foodplatform.common.Result;
import com.campus.foodplatform.dto.ShopDTO;
import com.campus.foodplatform.dto.ShopQueryDTO;
import com.campus.foodplatform.entity.Shop;
import com.campus.foodplatform.service.DiscountService;
import com.campus.foodplatform.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/shops")
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;
    private final DiscountService discountService;

    @GetMapping("/top/like")
    public Result<List<Shop>> hot(@RequestParam(defaultValue = "5") int size) {
        return Result.success(shopService.getHot(size));
    }

    @GetMapping("/list")
    public Result<PageResult<Shop>> list(ShopQueryDTO query) {
        return Result.success(shopService.list(query));
    }

    @GetMapping("/{id}")
    public Result<Shop> detail(@PathVariable Long id) {
        return Result.success(shopService.getById(id));
    }

    @GetMapping("/{id}/discounts")
    public Result<?> discounts(@PathVariable Long id) {
        return Result.success(discountService.getByShop(id));
    }

    @PostMapping
    public Result<Shop> create(@Valid @RequestBody ShopDTO dto) {
        return Result.success(shopService.create(dto));
    }

    @PutMapping("/{id}")
    public Result<Shop> update(@PathVariable Long id, @Valid @RequestBody ShopDTO dto) {
        return Result.success(shopService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        shopService.delete(id);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    public Result<Void> toggleStatus(@PathVariable Long id, @RequestParam Integer status) {
        shopService.toggleStatus(id, status);
        return Result.success();
    }
}
