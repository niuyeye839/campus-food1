package com.campus.foodplatform.mapper;

import com.campus.foodplatform.dto.ShopQueryDTO;
import com.campus.foodplatform.entity.Shop;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface ShopMapper {
        @Insert("INSERT INTO shop(merchant_id,name,category,address,latitude,longitude,phone,description,images,business_hours,student_discount,status,created_at,updated_at) "
                        +
                        "VALUES(#{merchantId},#{name},#{category},#{address},#{latitude},#{longitude},#{phone},#{description},#{images},#{businessHours},#{studentDiscount},0,NOW(),NOW())")
        @Options(useGeneratedKeys = true, keyProperty = "id")
        int insert(Shop shop);

        @Select("SELECT * FROM shop WHERE id=#{id}")
        Shop selectById(Long id);

        List<Shop> selectList(@Param("q") ShopQueryDTO query, @Param("offset") int offset);

        long countList(@Param("q") ShopQueryDTO query);

        @Update("UPDATE shop SET name=#{name},category=#{category},address=#{address},latitude=#{latitude}," +
                        "longitude=#{longitude},phone=#{phone},description=#{description},images=#{images}," +
                        "business_hours=#{businessHours},student_discount=#{studentDiscount},updated_at=NOW() WHERE id=#{id}")
        int update(Shop shop);

        @Update("UPDATE shop SET status=#{status},updated_at=NOW() WHERE id=#{id}")
        int updateStatus(@Param("id") Long id, @Param("status") Integer status);

        @Update("UPDATE shop SET score=#{score},updated_at=NOW() WHERE id=#{id}")
        int updateScore(@Param("id") Long id, @Param("score") BigDecimal score);

        @Update("UPDATE shop SET like_count=like_count+#{delta},updated_at=NOW() WHERE id=#{id}")
        int updateLikeCount(@Param("id") Long id, @Param("delta") int delta);

        @Update("UPDATE shop SET review_count=review_count+#{delta},updated_at=NOW() WHERE id=#{id}")
        int updateReviewCount(@Param("id") Long id, @Param("delta") int delta);

        @Delete("DELETE FROM shop WHERE id=#{id}")
        int deleteById(Long id);

        @Select("SELECT * FROM shop WHERE status=0 ORDER BY like_count DESC LIMIT #{limit}")
        List<Shop> selectTopByLike(@Param("limit") int limit);

        @Select("SELECT * FROM shop")
        List<Shop> selectAllForGeoFix();

        @Update("UPDATE shop SET longitude=#{lng}, latitude=#{lat}, updated_at=NOW() WHERE id=#{id}")
        int updateGeo(@Param("id") Long id, @Param("lng") BigDecimal lng, @Param("lat") BigDecimal lat);

        @Select("SELECT * FROM shop WHERE merchant_id=#{merchantId} ORDER BY id DESC LIMIT 1")
        Shop selectByMerchantId(@Param("merchantId") Long merchantId);
}
