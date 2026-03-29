package com.campus.foodplatform.service;

import com.campus.foodplatform.entity.Follow;
import com.campus.foodplatform.mapper.FollowMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowMapper followMapper;

    public boolean toggle(Long userId, String targetType, Long targetId) {
        int exists = followMapper.exists(userId, targetType, targetId);
        if (exists > 0) {
            followMapper.delete(userId, targetType, targetId);
            return false;
        } else {
            Follow follow = new Follow();
            follow.setUserId(userId);
            follow.setTargetType(targetType);
            follow.setTargetId(targetId);
            followMapper.insert(follow);
            return true;
        }
    }

    public boolean isFollowed(Long userId, String targetType, Long targetId) {
        return followMapper.exists(userId, targetType, targetId) > 0;
    }

    public List<Long> getFollowIds(Long userId, String targetType) {
        return followMapper.selectTargetIds(userId, targetType);
    }

    public List<Long> getFollowShopIds(Long userId) {
        return followMapper.selectTargetIds(userId, "SHOP");
    }
}
