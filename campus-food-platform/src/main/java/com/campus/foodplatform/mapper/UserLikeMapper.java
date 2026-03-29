package com.campus.foodplatform.mapper;

import com.campus.foodplatform.entity.UserLike;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserLikeMapper {
    @Insert("INSERT INTO user_like(user_id,target_type,target_id,created_at) VALUES(#{userId},#{targetType},#{targetId},NOW())")
    int insert(UserLike like);

    @Delete("DELETE FROM user_like WHERE user_id=#{userId} AND target_type=#{targetType} AND target_id=#{targetId}")
    int delete(@Param("userId") Long userId, @Param("targetType") String targetType, @Param("targetId") Long targetId);

    @Select("SELECT COUNT(*) FROM user_like WHERE user_id=#{userId} AND target_type=#{targetType} AND target_id=#{targetId}")
    int exists(@Param("userId") Long userId, @Param("targetType") String targetType, @Param("targetId") Long targetId);
}
