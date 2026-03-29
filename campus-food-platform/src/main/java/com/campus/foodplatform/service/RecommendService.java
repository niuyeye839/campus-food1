/** 个性化推荐服务，根据用户口味偏好和预算推荐合适店铺 */
package com.campus.foodplatform.service;

import com.campus.foodplatform.dto.ShopQueryDTO;
import com.campus.foodplatform.entity.Shop;
import com.campus.foodplatform.entity.User;
import com.campus.foodplatform.mapper.FavoriteMapper;
import com.campus.foodplatform.mapper.ShopMapper;
import com.campus.foodplatform.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecommendService {

    private final ShopMapper shopMapper;
    private final UserMapper userMapper;
    private final FavoriteMapper favoriteMapper;

    /**
     * 个性化推荐：结合口味偏好、预算、浏览/收藏历史
     */
    public List<Shop> recommend(Long userId, int size) {
        User user = userMapper.selectById(userId);
        ShopQueryDTO query = new ShopQueryDTO();
        query.setPage(1);
        query.setSize(size);
        query.setSortBy("score");

        // 根据口味偏好设置分类
        if (user != null && StringUtils.hasText(user.getTastePreference())) {
            String[] prefs = user.getTastePreference().split(",");
            if (prefs.length > 0) {
                query.setCategory(prefs[0]); // 取第一个偏好分类
            }
        }

        // 根据预算筛选（此处简化，实际可扩展价格区间字段）
        return shopMapper.selectList(query, 0);
    }
}
