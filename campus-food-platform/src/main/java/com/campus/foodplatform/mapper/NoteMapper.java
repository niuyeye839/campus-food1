package com.campus.foodplatform.mapper;

import com.campus.foodplatform.entity.Note;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface NoteMapper {
    @Insert("INSERT INTO note(user_id,shop_id,title,content,type,media_urls,tags,like_count,comment_count,view_count,status,created_at,updated_at) " +
            "VALUES(#{userId},#{shopId},#{title},#{content},#{type},#{mediaUrls},#{tags},0,0,0,#{status},NOW(),NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Note note);

    @Select("SELECT * FROM note WHERE id=#{id}")
    Note selectById(Long id);

    List<Note> selectList(@Param("shopId") Long shopId, @Param("userId") Long userId,
                          @Param("status") Integer status, @Param("keyword") String keyword,
                          @Param("offset") int offset, @Param("size") int size);

    long countList(@Param("shopId") Long shopId, @Param("userId") Long userId,
                   @Param("status") Integer status, @Param("keyword") String keyword);

    @Update("UPDATE note SET title=#{title},content=#{content},media_urls=#{mediaUrls},tags=#{tags},status=#{status},updated_at=NOW() WHERE id=#{id}")
    int update(Note note);

    @Update("UPDATE note SET status=#{status},updated_at=NOW() WHERE id=#{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    @Update("UPDATE note SET like_count=like_count+#{delta} WHERE id=#{id}")
    int updateLikeCount(@Param("id") Long id, @Param("delta") int delta);

    @Update("UPDATE note SET comment_count=comment_count+#{delta} WHERE id=#{id}")
    int updateCommentCount(@Param("id") Long id, @Param("delta") int delta);

    @Update("UPDATE note SET view_count=view_count+1 WHERE id=#{id}")
    int incrementViewCount(Long id);

    @Delete("DELETE FROM note WHERE id=#{id}")
    int deleteById(Long id);
}
