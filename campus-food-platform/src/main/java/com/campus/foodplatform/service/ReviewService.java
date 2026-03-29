package com.campus.foodplatform.service;

import com.campus.foodplatform.common.BusinessException;
import com.campus.foodplatform.common.PageResult;
import com.campus.foodplatform.dto.ReviewDTO;
import com.campus.foodplatform.entity.Review;
import com.campus.foodplatform.entity.Shop;
import com.campus.foodplatform.mapper.MessageMapper;
import com.campus.foodplatform.mapper.ReviewMapper;
import com.campus.foodplatform.mapper.ShopMapper;
import com.campus.foodplatform.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewMapper reviewMapper;
    private final ShopMapper shopMapper;
    private final MessageMapper messageMapper;
    private final UserMapper userMapper;

    @Transactional
    public Review create(Long userId, ReviewDTO dto) {
        Review review = new Review();
        review.setUserId(userId);
        review.setShopId(dto.getShopId());
        review.setRating(dto.getRating());
        review.setContent(dto.getContent());
        review.setImages(dto.getImages());
        reviewMapper.insert(review);

        // 更新店铺评价数（待审核的也计入）
        shopMapper.updateReviewCount(dto.getShopId(), 1);
        
        // 给所有管理员发送消息通知
        sendReviewNotificationToAdmins(review);
        
        return review;
    }
    
    /**
     * 给所有管理员发送评价审核通知
     */
    private void sendReviewNotificationToAdmins(Review review) {
        // 获取所有管理员
        List<com.campus.foodplatform.entity.User> admins = userMapper.selectByRole("ADMIN");
        
        // 获取店铺信息
        Shop shop = shopMapper.selectById(review.getShopId());
        String shopName = shop != null ? shop.getName() : "未知店铺";
        
        // 给每个管理员发送消息
        for (com.campus.foodplatform.entity.User admin : admins) {
            com.campus.foodplatform.entity.Message message = new com.campus.foodplatform.entity.Message();
            message.setUserId(admin.getId());
            message.setType("SYSTEM");
            message.setTitle("新评价待审核");
            message.setContent("店铺「" + shopName + "」收到新评价，请及时审核");
            message.setRelatedId(review.getId());
            messageMapper.insert(message);
        }
    }

    public PageResult<Review> listByShop(Long shopId, int page, int size) {
        int offset = (page - 1) * size;
        List<Review> list = reviewMapper.selectByShopId(shopId, offset, size);
        long total = reviewMapper.countByShopId(shopId);
        return PageResult.of(total, list);
    }

    public PageResult<Review> listPublishedByShop(Long shopId, int page, int size) {
        int offset = (page - 1) * size;
        List<Review> list = reviewMapper.selectPublishedByShopId(shopId, offset, size);
        long total = reviewMapper.countPublishedByShopId(shopId);
        return PageResult.of(total, list);
    }

    /**
     * 获取店铺评价列表（包含当前用户自己的评价，即使被拒绝）
     */
    public PageResult<Review> listWithOwnReviews(Long shopId, Long userId, int page, int size) {
        int offset = (page - 1) * size;
        List<Review> list = reviewMapper.selectWithOwnReviews(shopId, userId, offset, size);
        long total = reviewMapper.countWithOwnReviews(shopId, userId);
        return PageResult.of(total, list);
    }

    public PageResult<Review> pendingList(int page, int size) {
        int offset = (page - 1) * size;
        return PageResult.of(reviewMapper.countPending(), reviewMapper.selectPendingList(offset, size));
    }

    @Transactional
    public void adminReview(Long reviewId, Integer status) {
        Review review = reviewMapper.selectById(reviewId);
        if (review == null)
            throw new BusinessException("评价不存在");
        reviewMapper.updateStatus(reviewId, status);
        if (status == 1) {
            refreshShopScore(review.getShopId());
        }
    }

    /** 重新计算并更新店铺加权评分 */
    private void refreshShopScore(Long shopId) {
        Double score = reviewMapper.calcWeightedScore(shopId);
        if (score != null) {
            shopMapper.updateScore(shopId, BigDecimal.valueOf(score));
        }
    }

    /**
     * 商家回复评价
     */
    @Transactional
    public void merchantReply(Long reviewId, Long merchantId, String content) {
        Review review = reviewMapper.selectById(reviewId);
        if (review == null) {
            throw new BusinessException("评价不存在");
        }

        // 验证商家是否有权限回复（检查是否是该店铺的商家）
        Shop shop = shopMapper.selectById(review.getShopId());
        if (shop == null) {
            throw new BusinessException("店铺不存在");
        }

        // 更新商家回复
        reviewMapper.updateMerchantReply(reviewId, content, LocalDateTime.now());
    }
}
