package com.campus.foodplatform.service;

import com.campus.foodplatform.common.PageResult;
import com.campus.foodplatform.entity.Favorite;
import com.campus.foodplatform.entity.Note;
import com.campus.foodplatform.entity.Shop;
import com.campus.foodplatform.mapper.FavoriteMapper;
import com.campus.foodplatform.mapper.NoteMapper;
import com.campus.foodplatform.mapper.ShopMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteMapper favoriteMapper;
    private final ShopMapper shopMapper;
    private final NoteMapper noteMapper;

    public boolean toggle(Long userId, String targetType, Long targetId) {
        int exists = favoriteMapper.exists(userId, targetType, targetId);
        if (exists > 0) {
            favoriteMapper.delete(userId, targetType, targetId);
            return false;
        } else {
            Favorite fav = new Favorite();
            fav.setUserId(userId);
            fav.setTargetType(targetType);
            fav.setTargetId(targetId);
            favoriteMapper.insert(fav);
            return true;
        }
    }

    public boolean isFavorited(Long userId, String targetType, Long targetId) {
        return favoriteMapper.exists(userId, targetType, targetId) > 0;
    }

    public PageResult<Shop> getFavoriteShops(Long userId, int page, int size) {
        int offset = (page - 1) * size;
        List<Long> ids = favoriteMapper.selectTargetIds(userId, "SHOP", offset, size);
        long total = favoriteMapper.countByUser(userId, "SHOP");
        List<Shop> shops = ids.stream().map(shopMapper::selectById).collect(Collectors.toList());
        return PageResult.of(total, shops);
    }

    public PageResult<Note> getFavoriteNotes(Long userId, int page, int size) {
        int offset = (page - 1) * size;
        List<Long> ids = favoriteMapper.selectTargetIds(userId, "NOTE", offset, size);
        long total = favoriteMapper.countByUser(userId, "NOTE");
        List<Note> notes = ids.stream().map(noteMapper::selectById).collect(Collectors.toList());
        return PageResult.of(total, notes);
    }
}
