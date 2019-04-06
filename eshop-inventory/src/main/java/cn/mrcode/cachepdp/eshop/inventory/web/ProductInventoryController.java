package cn.mrcode.cachepdp.eshop.inventory.web;

import com.alibaba.fastjson.JSON;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.mrcode.cachepdp.eshop.inventory.model.ProductInventory;
import cn.mrcode.cachepdp.eshop.inventory.request.ProductInventoryCacheRefreshRequest;
import cn.mrcode.cachepdp.eshop.inventory.request.ProductInventoryDBUpdateRequest;
import cn.mrcode.cachepdp.eshop.inventory.service.ProductInventoryService;
import cn.mrcode.cachepdp.eshop.inventory.service.RequestAsyncProcessService;
import cn.mrcode.cachepdp.eshop.inventory.web.vo.Response;

/**
 * 商品库存
 *
 * @author : zhuqiang
 * @date : 2019/4/6 15:23
 */
@RestController
public class ProductInventoryController {
    @Autowired
    private RequestAsyncProcessService requestAsyncProcessService;
    @Autowired
    private ProductInventoryService productInventoryService;
    private Logger log = LoggerFactory.getLogger(getClass());

    /**
     * 更新商品库存
     */
    @RequestMapping("/updateProductInventory")
    public Response updateProductInventory(ProductInventory productInventory) {
        try {
            log.info("更新商品库存请求：商品id={}，库存={}", productInventory.getProductId(), productInventory.getInventoryCnt());
            ProductInventoryDBUpdateRequest request = new ProductInventoryDBUpdateRequest(productInventory, productInventoryService);
            requestAsyncProcessService.process(request);
            return new Response(Response.SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(Response.FAILURE);
        }
    }

    @RequestMapping("/getProductInventory")
    public ProductInventory getProductInventory(Integer productId) {
        try {
            log.info("读取商品库存请求：商品id={}", productId);
            // 异步获取
            ProductInventoryCacheRefreshRequest request = new ProductInventoryCacheRefreshRequest(productId, productInventoryService);
            requestAsyncProcessService.process(request);
            ProductInventory productInventory = null;

            long startTime = System.currentTimeMillis();
            long endTime = 0L;
            long waitTime = 0L;
            // 最多等待 200 毫秒
            while (true) {
                if (waitTime > 200) {
                    log.info("超时 200 毫秒退出尝试，商品 ID={}", productId);
                    break;
                }
                // 尝试去redis中读取一次商品库存的缓存数据
                productInventory = productInventoryService.getProductInventoryCache(productId);

                // 如果读取到了结果，那么就返回
                if (productInventory != null) {
                    log.info("在缓存中找到，商品 ID={}", productId);
                    return productInventory;
                }
                // 如果没有读取到结果，那么等待一段时间
                else {
                    Thread.sleep(20);
                    endTime = System.currentTimeMillis();
                    waitTime = endTime - startTime;
                }
            }
            // 直接尝试从数据库中读取数据
            productInventory = productInventoryService.findProductInventory(productId);
            log.info("缓存未命中，在数据库中查找，商品 ID={}，结果={}", productId, productInventory == null ? "数据库没有" : JSON.toJSONString(productInventory));
            if (productInventory != null) {
                // 读取到了数据，强制刷新缓存
                ProductInventoryCacheRefreshRequest forceRfreshRequest = new ProductInventoryCacheRefreshRequest(productId, productInventoryService, true);
                requestAsyncProcessService.process(forceRfreshRequest);
                return productInventory;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ProductInventory(productId, -1L);
    }
}
