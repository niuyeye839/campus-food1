/** 店铺服务，处理店铺的增删改查和状态管理业务逻辑 */
package com.campus.foodplatform.service;

import com.campus.foodplatform.common.BusinessException;
import com.campus.foodplatform.common.PageResult;
import com.campus.foodplatform.dto.ShopDTO;
import com.campus.foodplatform.dto.ShopQueryDTO;
import com.campus.foodplatform.entity.Shop;
import com.campus.foodplatform.mapper.ShopMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShopService {

    private final ShopMapper shopMapper;

    public List<Shop> getHot(int size) {
        return shopMapper.selectTopByLike(size);
    }

    public Shop getById(Long id) {
        Shop shop = shopMapper.selectById(id);
        if (shop == null)
            throw new BusinessException("店铺不存在");
        return shop;
    }

    public Shop getByMerchantId(Long merchantId) {
        return shopMapper.selectByMerchantId(merchantId);
    }

    public PageResult<Shop> list(ShopQueryDTO query) {
        int offset = (query.getPage() - 1) * query.getSize();
        List<Shop> list = shopMapper.selectList(query, offset);
        long total = shopMapper.countList(query);
        return PageResult.of(total, list);
    }

    public Shop create(ShopDTO dto) {
        Long merchantId = com.campus.foodplatform.common.UserContext.getUserId();
        
        // 检查商家是否已有店铺
        Shop existingShop = shopMapper.selectByMerchantId(merchantId);
        if (existingShop != null) {
            throw new BusinessException("您已经创建过店铺，请直接编辑");
        }
        
        Shop shop = new Shop();
        copyDtoToShop(dto, shop);
        shop.setMerchantId(merchantId);
        // 新建店铺默认进入待审核，且下线状态
        shop.setReviewStatus(0);
        shop.setStatus(1);
        shopMapper.insert(shop);
        return shop;
    }

    public Shop update(Long id, ShopDTO dto) {
        Shop shop = shopMapper.selectById(id);
        if (shop == null)
            throw new BusinessException("店铺不存在");
        copyDtoToShop(dto, shop);
        shop.setId(id);
        // 商家修改店铺信息后重新进入待审核
        shop.setReviewStatus(0);
        shopMapper.update(shop);
        return shop;
    }

    public void delete(Long id) {
        shopMapper.deleteById(id);
    }

    public void toggleStatus(Long id, Integer status) {
        shopMapper.updateStatus(id, status);
    }

    public PageResult<Shop> adminPendingList(int page, int size) {
        int offset = (page - 1) * size;
        List<Shop> list = shopMapper.selectPending(offset, size);
        long total = shopMapper.countPending();
        return PageResult.of(total, list);
    }

    public void review(Long id, Integer reviewStatus, String reviewRemark) {
        Shop shop = shopMapper.selectById(id);
        if (shop == null) {
            throw new BusinessException("店铺不存在");
        }
        // 只有待审核的店铺允许审核
        if (shop.getReviewStatus() != null && shop.getReviewStatus() != 0) {
            throw new BusinessException("该店铺已审核，请勿重复操作");
        }
        // 审核通过时默认上线
        if (reviewStatus != null && reviewStatus == 1) {
            shopMapper.updateStatus(id, 0);
        }
        shopMapper.updateReviewStatus(id, reviewStatus, reviewRemark);
    }

    private void copyDtoToShop(ShopDTO dto, Shop shop) {
        shop.setName(dto.getName());
        shop.setCategory(dto.getCategory());
        shop.setAddress(dto.getAddress());
        shop.setLatitude(dto.getLatitude());
        shop.setLongitude(dto.getLongitude());
        shop.setPhone(dto.getPhone());
        shop.setDescription(dto.getDescription());
        shop.setImages(dto.getImages());
        shop.setBusinessHours(dto.getBusinessHours());
        shop.setStudentDiscount(dto.getStudentDiscount());
    }
}
