package com.campus.foodplatform.mapper;

import com.campus.foodplatform.entity.Review;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface ReviewMapper {
        @Insert("INSERT INTO review(user_id,shop_id,rating,content,images,like_count,status,created_at,updated_at) " +
                        "VALUES(#{userId},#{shopId},#{rating},#{content},#{images},0,1,NOW(),NOW())")
        @Options(useGeneratedKeys = true, keyProperty = "id")
        int insert(Review review);

        @Select("SELECT * FROM review WHERE id=#{id}")
        Review selectById(Long id);

        @Select("SELECT * FROM review WHERE shop_id=#{shopId} AND status=1 ORDER BY created_at DESC LIMIT #{offset},#{size}")
        List<Review> selectByShopId(@Param("shopId") Long shopId, @Param("offset") int offset, @Param("size") int size);

        @Select("SELECT COUNT(*) FROM review WHERE shop_id=#{shopId} AND status=1")
        long countByShopId(Long shopId);

        /** 计算加权评分：评分权重0.7 + 点赞权重0.3 */
        @Select("SELECT AVG(rating)*0.7 + (SUM(like_count)/NULLIF(COUNT(*),0))*0.3 FROM review WHERE shop_id=#{shopId} AND status=1")
        Double calcWeightedScore(Long shopId);

        @Select("SELECT AVG(rating) as avgRating, COUNT(*) as total FROM review WHERE shop_id=#{shopId} AND status=1")
        Map<String, Object> selectScoreStats(Long shopId);

        @Update("UPDATE review SET status=#{status},updated_at=NOW() WHERE id=#{id}")
        int updateStatus(@Param("id") Long id, @Param("status") Integer status);

        @Update("UPDATE review SET like_count=like_count+#{delta} WHERE id=#{id}")
        int updateLikeCount(@Param("id") Long id, @Param("delta") int delta);

        List<Review> selectPendingList(@Param("offset") int offset, @Param("size") int size);

        long countPending();

        @Update("UPDATE review SET merchant_reply=#{content},merchant_reply_time=#{replyTime},updated_at=NOW() WHERE id=#{id}")
        int updateMerchantReply(@Param("id") Long id, @Param("content") String content,
                        @Param("replyTime") java.time.LocalDateTime replyTime);
}
