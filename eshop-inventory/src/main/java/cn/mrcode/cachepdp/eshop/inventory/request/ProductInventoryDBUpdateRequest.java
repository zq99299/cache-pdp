package cn.mrcode.cachepdp.eshop.inventory.request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.mrcode.cachepdp.eshop.inventory.model.ProductInventory;
import cn.mrcode.cachepdp.eshop.inventory.service.ProductInventoryService;

/**
 * 数据更新请求
 *
 * @author : zhuqiang
 * @date : 2019/4/3 23:05
 */
public class ProductInventoryDBUpdateRequest implements Request {
    private ProductInventory productInventory;
    private ProductInventoryService productInventoryService;
    private Logger log = LoggerFactory.getLogger(getClass());

    public ProductInventoryDBUpdateRequest(ProductInventory productInventory, ProductInventoryService productInventoryService) {
        this.productInventory = productInventory;
        this.productInventoryService = productInventoryService;
    }

    @Override
    public void process() {
        //1. 删除缓存
        productInventoryService.removeProductInventoryCache(productInventory.getProductId());
//        log.info("写请求：模拟写耗时操作，休眠 10 秒钟");
//        try {
//            TimeUnit.SECONDS.sleep(5);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        //2. 更新库存
        productInventoryService.updateProductInventory(productInventory);
    }

    @Override
    public Integer getProductId() {
        return productInventory.getProductId();
    }

    @Override
    public boolean isForceRfresh() {
        return false;
    }
}
