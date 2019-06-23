package cn.mrcode.cachepdp.eshop.cache.service.impl;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import cn.mrcode.cachepdp.eshop.cache.command.GetProductInfoOfRedisCommand;
import cn.mrcode.cachepdp.eshop.cache.command.GetShopInfoOfRedisCommand;
import cn.mrcode.cachepdp.eshop.cache.command.SaveProductInfo2RedisCommand;
import cn.mrcode.cachepdp.eshop.cache.command.SaveShopInfo2RedisCommand;
import cn.mrcode.cachepdp.eshop.cache.model.ProductInfo;
import cn.mrcode.cachepdp.eshop.cache.model.ShopInfo;
import cn.mrcode.cachepdp.eshop.cache.service.CacheService;
import redis.clients.jedis.JedisCluster;

/**
 * ${todo}
 *
 * @author : zhuqiang
 * @date : 2019/4/7 10:15
 */
@Service
public class CacheServiceImpl implements CacheService {
    public static final String CACHE_NAME = "local";

    @Resource
    private JedisCluster jedisCluster;

    /**
     * 将商品信息保存到本地缓存中
     */
    @CachePut(value = CACHE_NAME, key = "'key_'+#productInfo.getId()")
    public ProductInfo saveLocalCache(ProductInfo productInfo) {
        return productInfo;
    }

    /**
     * 从本地缓存中获取商品信息
     */
    @Cacheable(value = CACHE_NAME, key = "'key_'+#id")
    public ProductInfo getLocalCache(Long id) {
        return null;
    }

    /**
     * 将商品信息保存到本地的ehcache缓存中
     */
    @CachePut(value = CACHE_NAME, key = "'product_info_'+#productInfo.getId()")
    public ProductInfo saveProductInfo2LocalCache(ProductInfo productInfo) {
        return productInfo;
    }

    /**
     * 从本地ehcache缓存中获取商品信息
     */
    @Cacheable(value = CACHE_NAME, key = "'product_info_'+#productId")
    public ProductInfo getProductInfoFromLocalCache(Long productId) {
        return null;
    }

    /**
     * 将店铺信息保存到本地的ehcache缓存中
     */
    @CachePut(value = CACHE_NAME, key = "'shop_info_'+#shopInfo.getId()")
    public ShopInfo saveShopInfo2LocalCache(ShopInfo shopInfo) {
        return shopInfo;
    }

    /**
     * 从本地ehcache缓存中获取店铺信息
     */
    @Cacheable(value = CACHE_NAME, key = "'shop_info_'+#shopId")
    public ShopInfo getShopInfoFromLocalCache(Long shopId) {
        return null;
    }

    /**
     * 将商品信息保存到redis中
     */
    public void saveProductInfo2RedisCache(ProductInfo productInfo) {
//        String key = "product_info_" + productInfo.getId();
//        jedisCluster.set(key, JSONObject.toJSONString(productInfo));
        SaveProductInfo2RedisCommand command = new SaveProductInfo2RedisCommand(productInfo);
        command.setJedisCluster(jedisCluster);
        command.execute();
    }

    /**
     * 将店铺信息保存到redis中
     */
    public void saveShopInfo2RedisCache(ShopInfo shopInfo) {
//        String key = "shop_info_" + shopInfo.getId();
//        jedisCluster.set(key, JSONObject.toJSONString(shopInfo));
        SaveShopInfo2RedisCommand command = new SaveShopInfo2RedisCommand(shopInfo);
        command.setJedisCluster(jedisCluster);
        command.execute();
    }

    @Override
    public ProductInfo getProductInfoOfRedisCache(Long productId) {
//        String key = "product_info_" + productId;
//        String json = jedisCluster.get(key);
//        return JSON.parseObject(json, ProductInfo.class);
        GetProductInfoOfRedisCommand command = new GetProductInfoOfRedisCommand(productId);
        command.setJedisCluster(jedisCluster);
        return command.execute();
    }

    @Override
    public ShopInfo getShopInfoOfRedisCache(Long shopId) {
//        String key = "shop_info_" + shopId;
//        String json = jedisCluster.get(key);
//        return JSON.parseObject(json, ShopInfo.class);
        GetShopInfoOfRedisCommand command = new GetShopInfoOfRedisCommand(shopId);
        command.setJedisCluster(jedisCluster);
        return command.execute();
    }
}
