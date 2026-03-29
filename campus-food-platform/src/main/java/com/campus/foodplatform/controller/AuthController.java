/** 认证控制器，提供用户注册、登录和修改密码接口 */
package com.campus.foodplatform.controller;

import com.campus.foodplatform.common.Result;
import com.campus.foodplatform.common.UserContext;
import com.campus.foodplatform.dto.LoginDTO;
import com.campus.foodplatform.dto.RegisterDTO;
import com.campus.foodplatform.dto.ResetPasswordDTO;
import com.campus.foodplatform.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public Result<Map<String, Object>> register(@Valid @RequestBody RegisterDTO dto) {
        return Result.success(authService.register(dto));
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginDTO dto) {
        return Result.success(authService.login(dto));
    }

    @PostMapping("/change-password")
    public Result<Void> changePassword(@RequestParam String oldPassword,
                                       @RequestParam String newPassword) {
        authService.changePassword(UserContext.getUserId(), oldPassword, newPassword);
        return Result.success();
    }
}
