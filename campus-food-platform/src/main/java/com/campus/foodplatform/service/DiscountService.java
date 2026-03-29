package com.campus.foodplatform.service;

import com.campus.foodplatform.common.PageResult;
import com.campus.foodplatform.entity.Discount;
import com.campus.foodplatform.entity.Message;
import com.campus.foodplatform.mapper.DiscountMapper;
import com.campus.foodplatform.mapper.FollowMapper;
import com.campus.foodplatform.mapper.MessageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiscountService {

    private final DiscountMapper discountMapper;
    private final FollowMapper followMapper;
    private final MessageMapper messageMapper;

    public PageResult<Discount> list(int page, int size) {
        int offset = (page - 1) * size;
        return PageResult.of(discountMapper.countValid(), discountMapper.selectValid(offset, size));
    }

    public List<Discount> getByShop(Long shopId) {
        return discountMapper.selectByShopId(shopId);
    }

    /** 每天凌晨2点爬取优惠信息 */
    @Scheduled(cron = "0 0 2 * * ?")
    public void crawlDiscounts() {
        log.info("开始爬取优惠信息...");
        try {
            // 示例：爬取公开优惠页面（实际需根据目标网站调整选择器）
            Document doc = Jsoup.connect("https://example.com/discounts")
                    .userAgent("Mozilla/5.0")
                    .timeout(10000)
                    .get();
            Elements items = doc.select(".discount-item");
            for (Element item : items) {
                Discount discount = new Discount();
                discount.setTitle(item.select(".title").text());
                discount.setDescription(item.select(".desc").text());
                discount.setSource("example.com");
                discount.setStartTime(LocalDateTime.now());
                discount.setEndTime(LocalDateTime.now().plusDays(7));
                discountMapper.insert(discount);
            }
        } catch (Exception e) {
            log.warn("爬取优惠信息失败: {}", e.getMessage());
        }
        // 标记过期优惠
        discountMapper.expireDiscounts();
        log.info("优惠信息爬取完成");
    }

    /** 每天上午10点推送即将过期优惠提醒 */
    @Scheduled(cron = "0 0 10 * * ?")
    public void pushExpiringReminders() {
        List<Discount> expiring = discountMapper.selectExpiringSoon();
        for (Discount discount : expiring) {
            if (discount.getShopId() == null) continue;
            List<Long> followers = followMapper.selectFollowersByShop(discount.getShopId());
            for (Long userId : followers) {
                Message msg = new Message();
                msg.setUserId(userId);
                msg.setType("DISCOUNT");
                msg.setTitle("优惠即将过期");
                msg.setContent("您关注的店铺优惠「" + discount.getTitle() + "」将在明天过期，快去使用吧！");
                msg.setRelatedId(discount.getId());
                messageMapper.insert(msg);
            }
        }
    }

    /** 新优惠推送给关注用户 */
    public void pushNewDiscount(Discount discount) {
        if (discount.getShopId() == null) return;
        List<Long> followers = followMapper.selectFollowersByShop(discount.getShopId());
        for (Long userId : followers) {
            Message msg = new Message();
            msg.setUserId(userId);
            msg.setType("DISCOUNT");
            msg.setTitle("新优惠上线");
            msg.setContent("您关注的店铺有新优惠：" + discount.getTitle());
            msg.setRelatedId(discount.getId());
            messageMapper.insert(msg);
        }
    }
}
