/** 用户实体类，对应数据库 user 表 */
package com.campus.foodplatform.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class User {
    private Long id;
    /** 学号（用于登录） */
    private String studentId;
    /** 真实姓名 */
    private String realName;
    private String username;
    private String password;
    private String email;
    private String phone;
    private String avatar;
    /** USER / ADMIN */
    private String role;
    /** 0-正常 1-禁用 */
    private Integer status;
    /** 口味偏好，逗号分隔 */
    private String tastePreference;
    /** 预算范围 */
    private Integer budget;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
