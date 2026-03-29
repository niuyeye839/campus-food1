package com.campus.foodplatform.mapper;

import com.campus.foodplatform.entity.Comment;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CommentMapper {
    @Insert("INSERT INTO comment(note_id,user_id,parent_id,reply_user_id,content,like_count,status,created_at) " +
            "VALUES(#{noteId},#{userId},#{parentId},#{replyUserId},#{content},0,1,NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Comment comment);

    @Select("SELECT * FROM comment WHERE id=#{id}")
    Comment selectById(Long id);

    @Select("SELECT * FROM comment WHERE note_id=#{noteId} AND parent_id IS NULL AND status=1 ORDER BY created_at DESC LIMIT #{offset},#{size}")
    List<Comment> selectTopByNoteId(@Param("noteId") Long noteId, @Param("offset") int offset, @Param("size") int size);

    @Select("SELECT * FROM comment WHERE parent_id=#{parentId} AND status=1 ORDER BY created_at ASC")
    List<Comment> selectReplies(Long parentId);

    @Select("SELECT COUNT(*) FROM comment WHERE note_id=#{noteId} AND status=1")
    long countByNoteId(Long noteId);

    @Update("UPDATE comment SET like_count=like_count+#{delta} WHERE id=#{id}")
    int updateLikeCount(@Param("id") Long id, @Param("delta") int delta);

    @Update("UPDATE comment SET status=#{status} WHERE id=#{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    @Delete("DELETE FROM comment WHERE id=#{id}")
    int deleteById(Long id);
}
