/** 收藏夹控制器 */
package com.campus.foodplatform.controller;

import com.campus.foodplatform.common.Result;
import com.campus.foodplatform.common.UserContext;
import com.campus.foodplatform.entity.FavoriteFolder;
import com.campus.foodplatform.service.FavoriteFolderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorite-folders")
@RequiredArgsConstructor
public class FavoriteFolderController {

    private final FavoriteFolderService folderService;

    @GetMapping
    public Result<List<FavoriteFolder>> list() {
        return Result.success(folderService.getUserFolders(UserContext.getUserId()));
    }

    @PostMapping
    public Result<FavoriteFolder> create(@RequestParam String name) {
        return Result.success(folderService.create(UserContext.getUserId(), name));
    }

    @PutMapping("/{id}")
    public Result<Void> rename(@PathVariable Long id, @RequestParam String name) {
        folderService.rename(UserContext.getUserId(), id, name);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        folderService.delete(UserContext.getUserId(), id);
        return Result.success();
    }
}
