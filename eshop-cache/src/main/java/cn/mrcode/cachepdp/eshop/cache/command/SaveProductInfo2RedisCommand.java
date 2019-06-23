package cn.mrcode.cachepdp.eshop.cache.command;

import com.alibaba.fastjson.JSONObject;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolProperties;

import cn.mrcode.cachepdp.eshop.cache.model.ProductInfo;
import redis.clients.jedis.JedisCluster;

/**
 * @author : zhuqiang
 * @date : 2019/6/23 15:07
 */
public class SaveProductInfo2RedisCommand extends HystrixCommand<Boolean> {
    private JedisCluster jedisCluster;
    private final ProductInfo productInfo;

    public SaveProductInfo2RedisCommand(ProductInfo productInfo) {
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("SaveProductInfo2RedisCommand"))
                .andCommandPropertiesDefaults(
                        HystrixCommandProperties.Setter()
                                .withExecutionTimeoutInMilliseconds(100)
                                .withCircuitBreakerRequestVolumeThreshold(20)
                                .withCircuitBreakerErrorThresholdPercentage(80)
                                .withCircuitBreakerSleepWindowInMilliseconds(1000 * 60)
                )
        );
        this.productInfo = productInfo;
    }

    @Override
    protected Boolean run() throws Exception {
        String key = "product_info_" + productInfo.getId();
        jedisCluster.set(key, JSONObject.toJSONString(productInfo));
        return true;
    }

    public JedisCluster getJedisCluster() {
        return jedisCluster;
    }

    public void setJedisCluster(JedisCluster jedisCluster) {
        this.jedisCluster = jedisCluster;
    }
}
