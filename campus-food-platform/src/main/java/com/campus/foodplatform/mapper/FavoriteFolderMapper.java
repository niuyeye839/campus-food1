/** 收藏夹 Mapper 接口 */
package com.campus.foodplatform.mapper;

import com.campus.foodplatform.entity.FavoriteFolder;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface FavoriteFolderMapper {
    
    @Insert("INSERT INTO favorite_folder(user_id, name, is_default, created_at, updated_at) " +
            "VALUES(#{userId}, #{name}, #{isDefault}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(FavoriteFolder folder);
    
    @Select("SELECT * FROM favorite_folder WHERE id = #{id}")
    FavoriteFolder selectById(Long id);
    
    @Select("SELECT * FROM favorite_folder WHERE user_id = #{userId} ORDER BY is_default DESC, created_at ASC")
    List<FavoriteFolder> selectByUserId(Long userId);
    
    @Select("SELECT * FROM favorite_folder WHERE user_id = #{userId} AND is_default = 1")
    FavoriteFolder selectDefaultByUserId(Long userId);
    
    @Update("UPDATE favorite_folder SET name = #{name}, updated_at = NOW() WHERE id = #{id}")
    int update(FavoriteFolder folder);
    
    @Delete("DELETE FROM favorite_folder WHERE id = #{id} AND is_default = 0")
    int delete(Long id);
    
    @Select("SELECT COUNT(*) FROM favorite WHERE folder_id = #{folderId}")
    int countFavorites(Long folderId);
}
