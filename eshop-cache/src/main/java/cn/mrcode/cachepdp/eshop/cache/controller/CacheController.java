package cn.mrcode.cachepdp.eshop.cache.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.mrcode.cachepdp.eshop.cache.model.ProductInfo;
import cn.mrcode.cachepdp.eshop.cache.model.ShopInfo;
import cn.mrcode.cachepdp.eshop.cache.service.CacheService;

/**
 * 缓存Controller
 *
 * @author Administrator
 */
@Controller
public class CacheController {
    private Logger log = LoggerFactory.getLogger(getClass());
    @Autowired
    private CacheService cacheService;

    @RequestMapping("/testPutCache")
    @ResponseBody
    public String testPutCache(ProductInfo productInfo) {
        cacheService.saveLocalCache(productInfo);
        return "success";
    }

    @RequestMapping("/testGetCache")
    @ResponseBody
    public ProductInfo testGetCache(Long id) {
        return cacheService.getLocalCache(id);
    }

    /**
     * 这里的代码别看着奇怪，简单回顾下之前的流程： 1. nginx 获取 redis 缓存 2. 获取不到再获取服务的堆缓存（也就是这里的 ecache） 3.
     * 还获取不到就需要去数据库获取并重建缓存
     */
    @RequestMapping("/getProductInfo")
    @ResponseBody
    public ProductInfo getProductInfo(Long productId) {
        ProductInfo productInfo = cacheService.getProductInfoOfReidsCache(productId);
        log.info("从 redis 中获取商品信息");
        if (productInfo == null) {
            productInfo = cacheService.getProductInfoFromLocalCache(productId);
            log.info("从 ehcache 中获取商品信息");
        }
        if (productInfo == null) {
            // 两级缓存中都获取不到数据，那么就需要从数据源重新拉取数据，重建缓存
            // 但是这里暂时不讲
            log.info("缓存重建 商品信息");
        }
        return productInfo;
    }

    @RequestMapping("/getShopInfo")
    @ResponseBody
    public ShopInfo getShopInfo(Long shopId) {
        ShopInfo shopInfo = cacheService.getShopInfoOfReidsCache(shopId);
        log.info("从 redis 中获取店铺信息");
        if (shopInfo == null) {
            shopInfo = cacheService.getShopInfoFromLocalCache(shopId);
            log.info("从 ehcache 中获取店铺信息");
        }
        if (shopInfo == null) {
            // 两级缓存中都获取不到数据，那么就需要从数据源重新拉取数据，重建缓存
            // 但是这里暂时不讲
            log.info("缓存重建 店铺信息");
        }
        return shopInfo;
    }

}
