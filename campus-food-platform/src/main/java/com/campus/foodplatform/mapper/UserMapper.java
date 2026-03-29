/** 用户 Mapper 接口，提供用户表的增删改查 SQL 映射 */
package com.campus.foodplatform.mapper;

import com.campus.foodplatform.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {
    @Insert("INSERT INTO `user`(student_id,real_name,username,password,email,phone,avatar,role,`status`,created_at,updated_at) " +
            "VALUES(#{studentId},#{realName},#{username},#{password},#{email},#{phone},#{avatar},#{role},#{status},NOW(),NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    @Select("SELECT * FROM `user` WHERE id=#{id}")
    User selectById(Long id);

    @Select("SELECT * FROM `user` WHERE student_id=#{studentId}")
    User selectByStudentId(String studentId);

    @Select("SELECT * FROM `user` WHERE username=#{username}")
    User selectByUsername(String username);

    @Select("SELECT * FROM `user` WHERE email=#{email}")
    User selectByEmail(String email);

    @Update("UPDATE `user` SET password=#{password},updated_at=NOW() WHERE id=#{id}")
    int updatePassword(@Param("id") Long id, @Param("password") String password);

    @Update("UPDATE `user` SET `status`=#{status},updated_at=NOW() WHERE id=#{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    @Update("UPDATE `user` SET avatar=#{avatar},taste_preference=#{tastePreference},budget=#{budget},updated_at=NOW() WHERE id=#{id}")
    int updateProfile(User user);

    List<User> selectList(@Param("keyword") String keyword, @Param("status") Integer status,
                          @Param("offset") int offset, @Param("size") int size);

    long countList(@Param("keyword") String keyword, @Param("status") Integer status);
}
