/** 收藏夹实体类，对应数据库 favorite_folder 表 */
package com.campus.foodplatform.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FavoriteFolder {
    private Long id;
    private Long userId;
    private String name;
    /** 是否默认收藏夹 */
    private Integer isDefault;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /** 收藏数量（关联查询） */
    private Integer count;
}
