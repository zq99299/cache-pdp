package cn.mrcode.cachepdp.eshop.inventory.service.impl;

import com.alibaba.fastjson.JSON;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.mrcode.cachepdp.eshop.inventory.dao.RedisDAO;
import cn.mrcode.cachepdp.eshop.inventory.mapper.ProductInventoryMapper;
import cn.mrcode.cachepdp.eshop.inventory.model.ProductInventory;
import cn.mrcode.cachepdp.eshop.inventory.service.ProductInventoryService;

/**
 * ${todo}
 *
 * @author : zhuqiang
 * @date : 2019/4/6 13:49
 */
@Service
public class ProductInventoryServiceImpl implements ProductInventoryService {
    @Autowired
    private ProductInventoryMapper productInventoryMapper;
    @Autowired
    private RedisDAO redisDAO;
    private Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public void updateProductInventory(ProductInventory productInventory) {
        productInventoryMapper.updateProductInventory(productInventory);
        log.info("已更新数据库：商品 ID={}，库存={}", productInventory.getProductId(), productInventory.getInventoryCnt());
    }

    @Override
    public ProductInventory findProductInventory(Integer productId) {
        log.info("数据库获取商品，商品 ID={}", productId);
        return productInventoryMapper.selectProductInventory(productId);
    }

    @Override
    public void removeProductInventoryCache(Integer productId) {
        redisDAO.del(buildKey(productId));
        log.info("已删除缓存：商品 ID={}", productId);
    }

    @Override
    public void setProductInventoryCache(ProductInventory productInventory) {
        log.info("设置缓存：{}", JSON.toJSONString(productInventory));
        redisDAO.set(buildKey(productInventory.getProductId()), productInventory.getInventoryCnt() + "");
    }

    @Override
    public ProductInventory getProductInventoryCache(Integer productId) {
        String result = redisDAO.get(buildKey(productId));

        if (result != null && !"".equals(result)) {
            try {
                Long inventoryCnt = Long.valueOf(result);
                return new ProductInventory(productId, inventoryCnt);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static String buildKey(Integer productId) {
        return "product:inventory:" + productId;
    }
}
