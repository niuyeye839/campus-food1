package com.campus.foodplatform.service;

import com.campus.foodplatform.common.BusinessException;
import com.campus.foodplatform.entity.UserLike;
import com.campus.foodplatform.mapper.CommentMapper;
import com.campus.foodplatform.mapper.NoteMapper;
import com.campus.foodplatform.mapper.ReviewMapper;
import com.campus.foodplatform.mapper.UserLikeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final UserLikeMapper userLikeMapper;
    private final NoteMapper noteMapper;
    private final ReviewMapper reviewMapper;
    private final CommentMapper commentMapper;

    @Transactional
    public boolean toggle(Long userId, String targetType, Long targetId) {
        int exists = userLikeMapper.exists(userId, targetType, targetId);
        if (exists > 0) {
            userLikeMapper.delete(userId, targetType, targetId);
            updateLikeCount(targetType, targetId, -1);
            return false;
        } else {
            UserLike like = new UserLike();
            like.setUserId(userId);
            like.setTargetType(targetType);
            like.setTargetId(targetId);
            userLikeMapper.insert(like);
            updateLikeCount(targetType, targetId, 1);
            return true;
        }
    }

    public boolean isLiked(Long userId, String targetType, Long targetId) {
        return userLikeMapper.exists(userId, targetType, targetId) > 0;
    }

    private void updateLikeCount(String targetType, Long targetId, int delta) {
        switch (targetType) {
            case "NOTE":    noteMapper.updateLikeCount(targetId, delta); break;
            case "REVIEW":  reviewMapper.updateLikeCount(targetId, delta); break;
            case "COMMENT": commentMapper.updateLikeCount(targetId, delta); break;
            default: throw new BusinessException("不支持的点赞类型");
        }
    }
}
