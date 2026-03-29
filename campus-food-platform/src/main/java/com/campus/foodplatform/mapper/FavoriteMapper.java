package com.campus.foodplatform.mapper;

import com.campus.foodplatform.entity.Favorite;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface FavoriteMapper {
    @Insert("INSERT INTO favorite(user_id, folder_id, target_type, target_id, created_at) " +
            "VALUES(#{userId}, #{folderId}, #{targetType}, #{targetId}, NOW())")
    int insert(Favorite favorite);

    @Delete("DELETE FROM favorite WHERE user_id=#{userId} AND target_type=#{targetType} AND target_id=#{targetId}")
    int delete(@Param("userId") Long userId, @Param("targetType") String targetType, @Param("targetId") Long targetId);

    @Select("SELECT COUNT(*) FROM favorite WHERE user_id=#{userId} AND target_type=#{targetType} AND target_id=#{targetId}")
    int exists(@Param("userId") Long userId, @Param("targetType") String targetType, @Param("targetId") Long targetId);

    @Select("SELECT target_id FROM favorite WHERE user_id=#{userId} AND target_type=#{targetType} " +
            "AND (#{folderId} IS NULL OR folder_id=#{folderId}) " +
            "ORDER BY created_at DESC LIMIT #{offset},#{size}")
    List<Long> selectTargetIds(@Param("userId") Long userId, @Param("targetType") String targetType,
                               @Param("folderId") Long folderId, @Param("offset") int offset, @Param("size") int size);

    @Select("SELECT COUNT(*) FROM favorite WHERE user_id=#{userId} AND target_type=#{targetType} " +
            "AND (#{folderId} IS NULL OR folder_id=#{folderId})")
    long countByUser(@Param("userId") Long userId, @Param("targetType") String targetType, @Param("folderId") Long folderId);
}
