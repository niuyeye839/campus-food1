package com.campus.foodplatform.mapper;

import com.campus.foodplatform.entity.Discount;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface DiscountMapper {
    @Insert("INSERT INTO discount(shop_id,title,description,original_url,source,start_time,end_time,status,created_at) " +
            "VALUES(#{shopId},#{title},#{description},#{originalUrl},#{source},#{startTime},#{endTime},0,NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Discount discount);

    @Select("SELECT * FROM discount WHERE status=0 ORDER BY created_at DESC LIMIT #{offset},#{size}")
    List<Discount> selectValid(@Param("offset") int offset, @Param("size") int size);

    @Select("SELECT COUNT(*) FROM discount WHERE status=0")
    long countValid();

    @Select("SELECT * FROM discount WHERE shop_id=#{shopId} AND status=0")
    List<Discount> selectByShopId(Long shopId);

    /** 标记过期 */
    @Update("UPDATE discount SET status=1 WHERE end_time < NOW() AND status=0")
    int expireDiscounts();

    /** 查询明天过期的优惠（用于推送提醒） */
    @Select("SELECT * FROM discount WHERE status=0 AND end_time BETWEEN NOW() AND DATE_ADD(NOW(), INTERVAL 1 DAY)")
    List<Discount> selectExpiringSoon();
}
