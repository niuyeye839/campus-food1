package com.campus.foodplatform.mapper;

import com.campus.foodplatform.entity.Message;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface MessageMapper {
    @Insert("INSERT INTO message(user_id,type,title,content,related_id,is_read,created_at) " +
            "VALUES(#{userId},#{type},#{title},#{content},#{relatedId},0,NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Message message);

    @Select("SELECT * FROM message WHERE user_id=#{userId} AND type=#{type} ORDER BY created_at DESC LIMIT #{offset},#{size}")
    List<Message> selectByType(@Param("userId") Long userId, @Param("type") String type,
                               @Param("offset") int offset, @Param("size") int size);

    @Select("SELECT COUNT(*) FROM message WHERE user_id=#{userId} AND is_read=0")
    int countUnread(Long userId);

    @Update("UPDATE message SET is_read=1 WHERE user_id=#{userId} AND type=#{type}")
    int markReadByType(@Param("userId") Long userId, @Param("type") String type);

    @Update("UPDATE message SET is_read=1 WHERE id=#{id}")
    int markRead(Long id);
}
