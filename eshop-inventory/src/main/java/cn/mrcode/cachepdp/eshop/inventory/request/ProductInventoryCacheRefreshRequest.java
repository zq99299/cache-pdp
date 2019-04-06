package cn.mrcode.cachepdp.eshop.inventory.request;

import cn.mrcode.cachepdp.eshop.inventory.model.ProductInventory;
import cn.mrcode.cachepdp.eshop.inventory.service.ProductInventoryService;

/**
 * 缓存刷新请求
 *
 * @author : zhuqiang
 * @date : 2019/4/6 14:13
 */
public class ProductInventoryCacheRefreshRequest implements Request {

    private Integer productId;
    private ProductInventoryService productInventoryService;
    private boolean forceRfresh = false;

    public ProductInventoryCacheRefreshRequest(Integer productId, ProductInventoryService productInventoryService) {
        this.productId = productId;
        this.productInventoryService = productInventoryService;
    }

    public ProductInventoryCacheRefreshRequest(Integer productId, ProductInventoryService productInventoryService, boolean forceRfresh) {
        this.productId = productId;
        this.productInventoryService = productInventoryService;
        this.forceRfresh = forceRfresh;
    }

    @Override
    public void process() {
        // 1. 读取数据库库存
        ProductInventory productInventory = productInventoryService.findProductInventory(productId);
        // 2. 设置缓存
        productInventoryService.setProductInventoryCache(productInventory);
    }

    @Override
    public Integer getProductId() {
        return productId;
    }

    @Override
    public boolean isForceRfresh() {
        return forceRfresh;
    }
}
