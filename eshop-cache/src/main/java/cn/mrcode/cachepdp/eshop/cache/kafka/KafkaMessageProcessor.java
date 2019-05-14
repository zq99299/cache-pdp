package cn.mrcode.cachepdp.eshop.cache.kafka;

import com.alibaba.fastjson.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import cn.mrcode.cachepdp.eshop.cache.ZooKeeperSession;
import cn.mrcode.cachepdp.eshop.cache.model.ProductInfo;
import cn.mrcode.cachepdp.eshop.cache.model.ShopInfo;
import cn.mrcode.cachepdp.eshop.cache.service.CacheService;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.message.MessageAndMetadata;

/**
 * @author : zhuqiang
 * @date : 2019/4/7 16:29
 */
public class KafkaMessageProcessor implements Runnable {

    private KafkaStream kafkaStream;
    private CacheService cacheService;
    private Logger log = LoggerFactory.getLogger(getClass());

    public KafkaMessageProcessor(KafkaStream kafkaStream, CacheService cacheService) {
        this.kafkaStream = kafkaStream;
        this.cacheService = cacheService;
    }

    public void run() {
        ConsumerIterator<byte[], byte[]> it = kafkaStream.iterator();
        while (it.hasNext()) {
            MessageAndMetadata<byte[], byte[]> next = it.next();
            String message = new String(next.message());

            // 首先将message转换成json对象
            JSONObject messageJSONObject = null;
            try {
                messageJSONObject = JSONObject.parseObject(message);
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
            // 从这里提取出消息对应的服务的标识
            String serviceId = messageJSONObject.getString("serviceId");

            // 如果是商品信息服务
            if ("productInfoService".equals(serviceId)) {
                processProductInfoChangeMessage(messageJSONObject);
            } else if ("shopInfoService".equals(serviceId)) {
                processShopInfoChangeMessage(messageJSONObject);
            }
        }
    }

    /**
     * 处理商品信息变更的消息
     */
    private void processProductInfoChangeMessage(JSONObject messageJSONObject) {
        // 提取出商品id
        Long productId = messageJSONObject.getLong("productId");

        // 调用商品信息服务的接口
        // 直接用注释模拟：getProductInfo?productId=1，传递过去
        // 商品信息服务，一般来说就会去查询数据库，去获取productId=1的商品信息，然后返回回来

        // 增加了一个 modifyTime 字段，来比较数据修改先后顺序
        String productInfoJSON = "{\"id\": 1, \"name\": \"iphone7手机\", \"price\": 5599, \"pictureList\":\"a.jpg,b.jpg\", \"specification\": \"iphone7的规格\", \"service\": \"iphone7的售后服务\", \"color\": \"红色,白色,黑色\", \"size\": \"5.5\", \"shopId\": 1," +
                "\"modifyTime\":\"2019-05-13 22:00:00\"}";
        ProductInfo productInfo = JSONObject.parseObject(productInfoJSON, ProductInfo.class);

        // 加锁
        ZooKeeperSession zks = ZooKeeperSession.getInstance();
        zks.acquireDistributedLock(productId);
        try {
            log.info("kafka 休眠 10 秒");
            TimeUnit.SECONDS.sleep(10);
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
                    log.info("数据未变更过");
                }
            } else {
                // redis 中没有数据，直接放入
                cacheService.saveProductInfo2LocalCache(productInfo);
                log.info("获取刚保存到本地缓存的商品信息：" + cacheService.getProductInfoFromLocalCache(productId));
                cacheService.saveProductInfo2ReidsCache(productInfo);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // 最后释放锁
            zks.releaseDistributedLock(productId);
        }
    }

    /**
     * 处理店铺信息变更的消息
     */
    private void processShopInfoChangeMessage(JSONObject messageJSONObject) {
        // 提取出商品id
        Long productId = messageJSONObject.getLong("productId");
        Long shopId = messageJSONObject.getLong("shopId");
        // 这里也是模拟去数据库获取到了信息

        String shopInfoJSON = "{\"id\": 1, \"name\": \"小王的手机店\", \"level\": 5, \"goodCommentRate\":0.99}";
        ShopInfo shopInfo = JSONObject.parseObject(shopInfoJSON, ShopInfo.class);
        cacheService.saveShopInfo2LocalCache(shopInfo);
        log.info("获取刚保存到本地缓存的店铺信息：" + cacheService.getShopInfoFromLocalCache(shopId));
        cacheService.saveShopInfo2ReidsCache(shopInfo);
    }
}
