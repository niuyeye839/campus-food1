/** 文件上传控制器，处理笔记图片和视频的上传并返回访问 URL */
package com.campus.foodplatform.controller;

import com.campus.foodplatform.common.BusinessException;
import com.campus.foodplatform.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class UploadController {

    @Value("${app.upload.path}")
    private String uploadPath;

    private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024L; // 10MB
    private static final long MAX_VIDEO_SIZE = 200 * 1024 * 1024L; // 200MB

    private static final List<String> IMAGE_TYPES = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");
    private static final List<String> VIDEO_TYPES = Arrays.asList("mp4", "mov", "avi", "webm");

    /**
     * 上传笔记媒体文件（图片或视频），支持多文件
     * 返回可访问的 URL 列表
     */
    @PostMapping("/media")
    public Result<List<String>> uploadMedia(@RequestParam("files") MultipartFile[] files) throws IOException {
        if (files == null || files.length == 0) {
            throw new BusinessException("请选择文件");
        }
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            urls.add(saveFile(file));
        }
        return Result.success(urls);
    }

    private String saveFile(MultipartFile file) throws IOException {
        String originalName = file.getOriginalFilename();
        String ext = getExtension(originalName).toLowerCase();

        String subDir;
        if (IMAGE_TYPES.contains(ext)) {
            if (file.getSize() > MAX_IMAGE_SIZE) throw new BusinessException("图片不能超过10MB");
            subDir = "images/";
        } else if (VIDEO_TYPES.contains(ext)) {
            if (file.getSize() > MAX_VIDEO_SIZE) throw new BusinessException("视频不能超过200MB");
            subDir = "videos/";
        } else {
            throw new BusinessException("不支持的文件类型：" + ext);
        }

        // 将相对路径解析为绝对路径，避免 Tomcat 工作目录问题
        File baseDir = new File(uploadPath);
        if (!baseDir.isAbsolute()) {
            baseDir = new File(System.getProperty("user.dir"), uploadPath);
        }

        String filename = UUID.randomUUID() + "." + ext;
        File dest = new File(baseDir, subDir + filename);
        dest.getParentFile().mkdirs();
        file.transferTo(dest);
        return "/uploads/" + subDir + filename;
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf('.') + 1);
    }
}
