package cn.mrcode.cachepdp.eshop.inventory.service;

import cn.mrcode.cachepdp.eshop.inventory.model.ProductInventory;

/**
 * ${todo}
 *
 * @author : zhuqiang
 * @date : 2019/4/6 13:48
 */
public interface ProductInventoryService {
    /**
     * 更新商品库存
     */
    void updateProductInventory(ProductInventory ProductInventory);

    /**
     * 根据商品id查询商品库存
     *
     * @param productId 商品id
     * @return 商品库存
     */
    ProductInventory findProductInventory(Integer productId);

    /**
     * 删除商品库存缓存
     */
    void removeProductInventoryCache(Integer productId);

    /**
     * 设置商品库存的缓存
     */
    void setProductInventoryCache(ProductInventory productInventory);

    ProductInventory getProductInventoryCache(Integer productId);
}
