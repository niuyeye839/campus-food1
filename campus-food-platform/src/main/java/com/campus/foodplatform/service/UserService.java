package com.campus.foodplatform.service;

import com.campus.foodplatform.common.BusinessException;
import com.campus.foodplatform.common.PageResult;
import com.campus.foodplatform.entity.User;
import com.campus.foodplatform.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;

    public User getById(Long id) {
        User user = userMapper.selectById(id);
        if (user != null) user.setPassword(null); // 不返回密码
        return user;
    }

    public void updateProfile(Long userId, String tastePreference, Integer budget) {
        User user = new User();
        user.setId(userId);
        user.setTastePreference(tastePreference);
        user.setBudget(budget);
        userMapper.updateProfile(user);
    }

    public String uploadAvatar(Long userId, MultipartFile file, String uploadPath) throws IOException {
        String ext = getExtension(file.getOriginalFilename());
        String filename = UUID.randomUUID() + "." + ext;

        File baseDir = new File(uploadPath);
        if (!baseDir.isAbsolute()) {
            baseDir = new File(System.getProperty("user.dir"), uploadPath);
        }
        File dest = new File(baseDir, "avatar/" + filename);
        dest.getParentFile().mkdirs();
        file.transferTo(dest);

        String url = "/uploads/avatar/" + filename;
        User user = new User();
        user.setId(userId);
        user.setAvatar(url);
        userMapper.updateProfile(user);
        return url;
    }

    public PageResult<User> adminList(String keyword, Integer status, int page, int size) {
        int offset = (page - 1) * size;
        List<User> list = userMapper.selectList(keyword, status, offset, size);
        list.forEach(u -> u.setPassword(null));
        long total = userMapper.countList(keyword, status);
        return PageResult.of(total, list);
    }

    public void toggleStatus(Long userId, Integer status) {
        userMapper.updateStatus(userId, status);
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "jpg";
        return filename.substring(filename.lastIndexOf('.') + 1);
    }
}
