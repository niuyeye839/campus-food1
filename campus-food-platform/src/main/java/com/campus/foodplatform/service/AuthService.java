/** 认证服务，处理用户注册、登录和密码修改业务逻辑 */
package com.campus.foodplatform.service;

import cn.hutool.crypto.digest.BCrypt;
import com.campus.foodplatform.common.BusinessException;
import com.campus.foodplatform.config.JwtUtil;
import com.campus.foodplatform.dto.LoginDTO;
import com.campus.foodplatform.dto.RegisterDTO;
import com.campus.foodplatform.dto.ResetPasswordDTO;
import com.campus.foodplatform.entity.User;
import com.campus.foodplatform.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    public Map<String, Object> register(RegisterDTO dto) {
        if (userMapper.selectByStudentId(dto.getStudentId()) != null) {
            throw new BusinessException("账号已被注册");
        }

        User user = new User();
        user.setStudentId(dto.getStudentId());
        user.setRealName(dto.getRealName());
        user.setUsername(dto.getRealName()); // 用姓名作为显示名
        user.setPassword(BCrypt.hashpw(dto.getPassword()));
        user.setRole(dto.getRole());
        user.setStatus(0);
        userMapper.insert(user);

        return buildTokenResult(user);
    }

    public Map<String, Object> login(LoginDTO dto) {
        User user = userMapper.selectByStudentId(dto.getAccount());
        if (user == null) {
            user = userMapper.selectByUsername(dto.getAccount());
        }
        if (user == null || !BCrypt.checkpw(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("账号或密码错误");
        }
        if (user.getStatus() == 1) {
            throw new BusinessException(403, "账号已被禁用");
        }
        if (!dto.getRole().equals(user.getRole())) {
            throw new BusinessException(403, "账号角色不匹配");
        }
        return buildTokenResult(user);
    }

    public void resetPassword(ResetPasswordDTO dto) {
        User user = userMapper.selectByStudentId(dto.getEmail()); // email字段复用为studentId
        if (user == null)
            throw new BusinessException("学号未注册");
        userMapper.updatePassword(user.getId(), BCrypt.hashpw(dto.getNewPassword()));
    }

    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (!BCrypt.checkpw(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        userMapper.updatePassword(userId, BCrypt.hashpw(newPassword));
    }

    private Map<String, Object> buildTokenResult(User user) {
        String token = jwtUtil.generateToken(user.getId(), user.getRole());
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("role", user.getRole());
        result.put("avatar", user.getAvatar());
        return result;
    }
}
