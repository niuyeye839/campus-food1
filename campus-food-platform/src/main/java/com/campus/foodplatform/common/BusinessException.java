/** 业务异常类，用于封装可预期的业务错误信息和状态码 */
package com.campus.foodplatform.common;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final int code;

    public BusinessException(String message) {
        super(message);
        this.code = 400;
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
