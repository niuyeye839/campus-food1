/** 收藏夹服务 */
package com.campus.foodplatform.service;

import com.campus.foodplatform.common.BusinessException;
import com.campus.foodplatform.entity.FavoriteFolder;
import com.campus.foodplatform.mapper.FavoriteFolderMapper;
import com.campus.foodplatform.mapper.FavoriteMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteFolderService {

    private final FavoriteFolderMapper folderMapper;
    private final FavoriteMapper favoriteMapper;

    /** 获取用户的所有收藏夹（包含收藏数量） */
    public List<FavoriteFolder> getUserFolders(Long userId) {
        List<FavoriteFolder> folders = folderMapper.selectByUserId(userId);
        for (FavoriteFolder folder : folders) {
            folder.setCount(folderMapper.countFavorites(folder.getId()));
        }
        return folders;
    }

    /** 创建收藏夹 */
    public FavoriteFolder create(Long userId, String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new BusinessException("收藏夹名称不能为空");
        }
        FavoriteFolder folder = new FavoriteFolder();
        folder.setUserId(userId);
        folder.setName(name.trim());
        folder.setIsDefault(0);
        folderMapper.insert(folder);
        return folder;
    }

    /** 重命名收藏夹 */
    public void rename(Long userId, Long folderId, String newName) {
        FavoriteFolder folder = folderMapper.selectById(folderId);
        if (folder == null || !folder.getUserId().equals(userId)) {
            throw new BusinessException("收藏夹不存在");
        }
        if (folder.getIsDefault() == 1) {
            throw new BusinessException("默认收藏夹不能重命名");
        }
        folder.setName(newName.trim());
        folderMapper.update(folder);
    }

    /** 删除收藏夹 */
    @Transactional
    public void delete(Long userId, Long folderId) {
        FavoriteFolder folder = folderMapper.selectById(folderId);
        if (folder == null || !folder.getUserId().equals(userId)) {
            throw new BusinessException("收藏夹不存在");
        }
        if (folder.getIsDefault() == 1) {
            throw new BusinessException("默认收藏夹不能删除");
        }
        
        // 将该收藏夹中的收藏移到默认收藏夹
        FavoriteFolder defaultFolder = folderMapper.selectDefaultByUserId(userId);
        // 这里需要在 FavoriteMapper 中添加批量更新方法
        
        folderMapper.delete(folderId);
    }

    /** 获取或创建默认收藏夹 */
    public FavoriteFolder getOrCreateDefault(Long userId) {
        FavoriteFolder folder = folderMapper.selectDefaultByUserId(userId);
        if (folder == null) {
            folder = new FavoriteFolder();
            folder.setUserId(userId);
            folder.setName("默认收藏夹");
            folder.setIsDefault(1);
            folderMapper.insert(folder);
        }
        return folder;
    }
}
