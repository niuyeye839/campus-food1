package com.campus.foodplatform.service;

import com.campus.foodplatform.entity.User;
import com.campus.foodplatform.mapper.NoteMapper;
import com.campus.foodplatform.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class RankService {

    private final NoteMapper noteMapper;
    private final UserMapper userMapper;

    /**
     * 探店达人总榜：按笔记总点赞数排名
     */
    public List<Map<String, Object>> totalRank(int size) {
        // 查询发布笔记最多且点赞最高的用户
        // 简化实现：通过笔记聚合统计
        return buildRankData("total", size);
    }

    /**
     * 月度榜
     */
    public List<Map<String, Object>> monthlyRank(int size) {
        return buildRankData("monthly", size);
    }

    /**
     * 品类榜
     */
    public List<Map<String, Object>> categoryRank(String category, int size) {
        return buildRankData("category:" + category, size);
    }

    private List<Map<String, Object>> buildRankData(String type, int size) {
        // 实际项目中通过SQL聚合查询，此处返回结构示例
        List<Map<String, Object>> result = new ArrayList<>();
        // SELECT u.id, u.username, u.avatar, SUM(n.like_count) as totalLikes, COUNT(n.id) as noteCount
        // FROM user u JOIN note n ON u.id=n.user_id WHERE n.status=2 GROUP BY u.id ORDER BY totalLikes DESC LIMIT #{size}
        return result;
    }
}
