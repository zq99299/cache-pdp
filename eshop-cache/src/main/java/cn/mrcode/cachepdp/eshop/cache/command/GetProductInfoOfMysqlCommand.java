package cn.mrcode.cachepdp.eshop.cache.command;

import com.alibaba.fastjson.JSONObject;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixThreadPoolProperties;

import cn.mrcode.cachepdp.eshop.cache.model.ProductInfo;

/**
 * @author : zhuqiang
 * @date : 2019/6/23 15:17
 */
public class GetProductInfoOfMysqlCommand extends HystrixCommand<ProductInfo> {
    private Long productId;

    public GetProductInfoOfMysqlCommand(Long productId) {
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("GetProductInfoOfMysqlCommand"))
                .andThreadPoolPropertiesDefaults(
                        HystrixThreadPoolProperties.Setter()
                                .withCoreSize(30)
                                .withMaxQueueSize(5)
                )
        );
        this.productId = productId;
    }

    @Override
    protected ProductInfo run() throws Exception {
        String productInfoJSON = "{\"id\": 1, \"name\": \"iphone7手机\", \"price\": 5599, \"pictureList\":\"a.jpg,b.jpg\", \"specification\": \"iphone7的规格\", \"service\": \"iphone7的售后服务\", \"color\": \"红色,白色,黑色\", \"size\": \"5.5\", \"shopId\": 1," +
                "\"modifyTime\":\"2019-05-13 22:00:00\"}";
        ProductInfo productInfo = JSONObject.parseObject(productInfoJSON, ProductInfo.class);
        return productInfo;
    }

    @Override
    protected ProductInfo getFallback() {
        return null;
    }
}
