package cn.mrcode.cachepdp.eshop.cache.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.mrcode.cachepdp.eshop.cache.command.GetProductInfoOfMysqlCommand;
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
    @Autowired
    private RebuildCache rebuildCache;

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
        ProductInfo productInfo = cacheService.getProductInfoOfRedisCache(productId);
        log.info("从 redis 中获取商品信息");
        if (productInfo == null) {
            productInfo = cacheService.getProductInfoFromLocalCache(productId);
            log.info("从 ehcache 中获取商品信息");
        }
        if (productInfo == null) {
            // 两级缓存中都获取不到数据，那么就需要从数据源重新拉取数据，重建缓存
            // 假设这里从数据库中获取的数据
//            String productInfoJSON = "{\"id\": 1, \"name\": \"iphone7手机\", \"price\": 5599, \"pictureList\":\"a.jpg,b.jpg\", \"specification\": \"iphone7的规格\", \"service\": \"iphone7的售后服务\", \"color\": \"红色,白色,黑色\", \"size\": \"5.5\", \"shopId\": 1," +
//                    "\"modifyTime\":\"2019-05-13 22:00:00\"}";
//            productInfo = JSONObject.parseObject(productInfoJSON, ProductInfo.class);
            GetProductInfoOfMysqlCommand command = new GetProductInfoOfMysqlCommand(productId);
            productInfo = command.execute();
            rebuildCache.put(productInfo);
        }
        return productInfo;
    }

    @RequestMapping("/getShopInfo")
    @ResponseBody
    public ShopInfo getShopInfo(Long shopId) {
        ShopInfo shopInfo = cacheService.getShopInfoOfRedisCache(shopId);
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
