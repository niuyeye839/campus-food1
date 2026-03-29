/** 管理员控制器，提供用户管理和运营数据统计接口 */
package com.campus.foodplatform.controller;

import com.campus.foodplatform.common.BusinessException;
import com.campus.foodplatform.common.PageResult;
import com.campus.foodplatform.common.Result;
import com.campus.foodplatform.common.UserContext;
import com.campus.foodplatform.entity.User;
import com.campus.foodplatform.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    private void checkAdmin() {
        if (!"ADMIN".equals(UserContext.getUserRole())) {
            throw new BusinessException(403, "无权限");
        }
    }

    @GetMapping("/users")
    public Result<PageResult<User>> users(@RequestParam(required = false) String keyword,
                                           @RequestParam(required = false) Integer status,
                                           @RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "10") int size) {
        checkAdmin();
        return Result.success(userService.adminList(keyword, status, page, size));
    }

    @PutMapping("/users/{id}/status")
    public Result<Void> toggleUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        checkAdmin();
        userService.toggleStatus(id, status);
        return Result.success();
    }

    /** 运营数据统计（ECharts数据源） */
    @GetMapping("/stats")
    public Result<?> stats() {
        checkAdmin();
        // 返回各维度统计数据，前端用ECharts渲染
        Map<String, Object> stats = new HashMap<>();
        stats.put("message", "统计数据接口，需结合具体业务SQL实现");
        return Result.success(stats);
    }
}
