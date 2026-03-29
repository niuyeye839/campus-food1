/** 全局异常处理器，统一捕获并返回标准错误响应 */
package com.campus.foodplatform.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusiness(BusinessException e) {
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public Result<Void> handleValidation(Exception e) {
        String msg;
        if (e instanceof MethodArgumentNotValidException) {
            msg = ((MethodArgumentNotValidException) e).getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        } else {
            msg = ((BindException) e).getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        }
        return Result.error(400, msg);
    }

    @ExceptionHandler(java.io.IOException.class)
    public Result<Void> handleIOException(java.io.IOException e) {
        log.error("文件操作异常", e);
        return Result.error(500, "文件上传失败：" + e.getMessage());
    }

    @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
    public Result<Void> handleMaxUploadSize(org.springframework.web.multipart.MaxUploadSizeExceededException e) {
        return Result.error(400, "文件超过最大限制（图片10MB，视频200MB）");
    }

    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public Result<Void> handleDataIntegrity(org.springframework.dao.DataIntegrityViolationException e) {
        log.error("数据完整性异常", e);
        return Result.error(400, "数据校验失败，请检查输入内容");
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.error(500, "系统繁忙，请稍后重试");
    }
}