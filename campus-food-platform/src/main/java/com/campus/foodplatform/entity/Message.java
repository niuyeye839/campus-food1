/** 消息实体类，对应数据库 message 表，包含互动、优惠和系统消息 */
package com.campus.foodplatform.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Message {
    private Long id;
    private Long userId;
    /** INTERACTION / DISCOUNT / SYSTEM */
    private String type;
    private String title;
    private String content;
    private Long relatedId;
    /** 0-未读 1-已读 */
    private Integer isRead;
    private LocalDateTime createdAt;
}
