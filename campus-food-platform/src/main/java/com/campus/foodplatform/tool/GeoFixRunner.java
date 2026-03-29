package com.campus.foodplatform.tool;

import com.campus.foodplatform.entity.Shop;
import com.campus.foodplatform.mapper.ShopMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 启动时自动用高德 POI 搜索修正店铺坐标。
 * 仅在 spring.profiles.active=geofix 时运行，避免每次启动都调用。
 * 运行方式：java -jar xxx.jar --spring.profiles.active=geofix
 */
@Slf4j
@Component
@Profile("geofix")
@RequiredArgsConstructor
public class GeoFixRunner implements CommandLineRunner {

    private final ShopMapper shopMapper;

    @Value("${amap.key:bc8a0abeba7621e7558d042c7bdf0b58}")
    private String amapKey;

    private final RestTemplate restTemplate = new RestTemplate();

    // 搜索中心点：武汉工程大学流芳校区
    private static final String CENTER = "114.5700,30.4350";
    private static final String CITY   = "武汉";

    @Override
    public void run(String... args) throws Exception {
        // 查所有店铺（不限 status）
        List<Shop> shops = shopMapper.selectAllForGeoFix();
        log.info("开始修正坐标，共 {} 家店铺", shops.size());

        int success = 0, skip = 0, fail = 0;
        for (Shop shop : shops) {
            try {
                Thread.sleep(200); // 避免触发高德限流
                double[] lngLat = searchPoi(shop.getName());
                if (lngLat == null) {
                    log.warn("未找到 POI：{}", shop.getName());
                    fail++;
                    continue;
                }
                shop.setLongitude(BigDecimal.valueOf(lngLat[0]));
                shop.setLatitude(BigDecimal.valueOf(lngLat[1]));
                shopMapper.updateGeo(shop.getId(), shop.getLongitude(), shop.getLatitude());
                log.info("✓ {} → [{}, {}]", shop.getName(), lngLat[0], lngLat[1]);
                success++;
            } catch (Exception e) {
                log.error("处理 {} 失败: {}", shop.getName(), e.getMessage());
                fail++;
            }
        }
        log.info("坐标修正完成：成功 {}，未找到 {}，失败 {}", success, skip, fail);
    }

    @SuppressWarnings("unchecked")
    private double[] searchPoi(String name) {
        String url = "https://restapi.amap.com/v3/place/text"
                + "?key=" + amapKey
                + "&keywords=" + encode(name)
                + "&city=" + CITY
                + "&citylimit=true"
                + "&offset=1"
                + "&output=json";

        Map<String, Object> resp = restTemplate.getForObject(url, Map.class);
        if (resp == null || !"1".equals(String.valueOf(resp.get("status")))) return null;

        List<?> pois = (List<?>) resp.get("pois");
        if (pois == null || pois.isEmpty()) return null;

        Map<?, ?> poi = (Map<?, ?>) pois.get(0);
        String location = (String) poi.get("location"); // "lng,lat"
        if (location == null || !location.contains(",")) return null;

        String[] parts = location.split(",");
        return new double[]{ Double.parseDouble(parts[0]), Double.parseDouble(parts[1]) };
    }

    private String encode(String s) {
        try { return java.net.URLEncoder.encode(s, "UTF-8"); }
        catch (Exception e) { return s; }
    }
}
