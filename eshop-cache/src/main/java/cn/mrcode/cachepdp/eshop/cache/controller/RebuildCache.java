package cn.mrcode.cachepdp.eshop.cache.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;

import cn.mrcode.cachepdp.eshop.cache.ZooKeeperSession;
import cn.mrcode.cachepdp.eshop.cache.model.ProductInfo;
import cn.mrcode.cachepdp.eshop.cache.service.CacheService;

/**
 * 缓存重建；一个队列对应一个消费线程
 *
 * @author : zhuqiang
 * @date : 2019/5/14 21:06
 */
@Component
public class RebuildCache {
    private Logger log = LoggerFactory.getLogger(getClass());
    private ArrayBlockingQueue<ProductInfo> queue = new ArrayBlockingQueue<>(100);
    private CacheService cacheService;

    public RebuildCache(CacheService cacheService) {
        this.cacheService = cacheService;
        start();
    }

    public void put(ProductInfo productInfo) {
        try {
            queue.put(productInfo);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public ProductInfo take() {
        try {
            return queue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 启动一个线程来消费

    private void start() {
        new Thread(() -> {
            System.out.println("缓存重建线程开始运行");
            while (true) {
                try {
                    ProductInfo productInfo = queue.take();
                    Long productId = productInfo.getId();
                    ZooKeeperSession zks = ZooKeeperSession.getInstance();
                    zks.acquireDistributedLock(productId);
                    try {
                        // 先获取一次 redis ，防止其他实例已经放入数据了
                        ProductInfo existedProduct = cacheService.getProductInfoOfReidsCache(productId);
                        if (existedProduct != null) {
                            // 判定通过消息获取到的数据版本和 redis 中的谁最新
                            Date existedModifyTime = existedProduct.getModifyTime();
                            Date modifyTime = productInfo.getModifyTime();
                            // 如果本次获取到的修改时间大于 redis 中的，那么说明此数据是最新的，可以放入 redis 中
                            if (modifyTime.after(existedModifyTime)) {
                                cacheService.saveProductInfo2LocalCache(productInfo);
                                log.info("最新数据覆盖 redis 中的数据：" + cacheService.getProductInfoFromLocalCache(productId));
                                cacheService.saveProductInfo2ReidsCache(productInfo);
                            } else {
                                log.info("此次数据版本落后，放弃重建");
                            }
                        } else {
                            // redis 中没有数据，直接放入
                            cacheService.saveProductInfo2LocalCache(productInfo);
                            log.info("缓存重建成功：" + cacheService.getProductInfoFromLocalCache(productId));
                            cacheService.saveProductInfo2ReidsCache(productInfo);
                        }
                    } finally {
                        // 最后释放锁
                        zks.releaseDistributedLock(productId);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
