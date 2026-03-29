package com.campus.foodplatform.mapper;

import com.campus.foodplatform.entity.Follow;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface FollowMapper {
    @Insert("INSERT INTO follow(user_id,target_type,target_id,created_at) VALUES(#{userId},#{targetType},#{targetId},NOW())")
    int insert(Follow follow);

    @Delete("DELETE FROM follow WHERE user_id=#{userId} AND target_type=#{targetType} AND target_id=#{targetId}")
    int delete(@Param("userId") Long userId, @Param("targetType") String targetType, @Param("targetId") Long targetId);

    @Select("SELECT COUNT(*) FROM follow WHERE user_id=#{userId} AND target_type=#{targetType} AND target_id=#{targetId}")
    int exists(@Param("userId") Long userId, @Param("targetType") String targetType, @Param("targetId") Long targetId);

    @Select("SELECT target_id FROM follow WHERE user_id=#{userId} AND target_type=#{targetType}")
    List<Long> selectTargetIds(@Param("userId") Long userId, @Param("targetType") String targetType);

    /** 查询关注了某店铺的所有用户ID */
    @Select("SELECT user_id FROM follow WHERE target_type='SHOP' AND target_id=#{shopId}")
    List<Long> selectFollowersByShop(Long shopId);
}
