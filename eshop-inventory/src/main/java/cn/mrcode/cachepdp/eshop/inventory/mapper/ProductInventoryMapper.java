package cn.mrcode.cachepdp.eshop.inventory.mapper;

import cn.mrcode.cachepdp.eshop.inventory.model.ProductInventory;

/**
 * ${todo}
 *
 * @author : zhuqiang
 * @date : 2019/4/6 13:50
 */
public interface ProductInventoryMapper {
    void updateProductInventory(ProductInventory productInventory);

    ProductInventory selectProductInventory(Integer productId);
}
