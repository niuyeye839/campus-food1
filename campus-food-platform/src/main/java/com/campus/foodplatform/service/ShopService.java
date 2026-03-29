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

    public PageResult<Shop> list(ShopQueryDTO query) {
        int offset = (query.getPage() - 1) * query.getSize();
        List<Shop> list = shopMapper.selectList(query, offset);
        long total = shopMapper.countList(query);
        return PageResult.of(total, list);
    }

    public Shop create(ShopDTO dto) {
        Shop shop = new Shop();
        copyDtoToShop(dto, shop);
        shopMapper.insert(shop);
        return shop;
    }

    public Shop update(Long id, ShopDTO dto) {
        Shop shop = shopMapper.selectById(id);
        if (shop == null)
            throw new BusinessException("店铺不存在");
        copyDtoToShop(dto, shop);
        shop.setId(id);
        shopMapper.update(shop);
        return shop;
    }

    public void delete(Long id) {
        shopMapper.deleteById(id);
    }

    public void toggleStatus(Long id, Integer status) {
        shopMapper.updateStatus(id, status);
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
