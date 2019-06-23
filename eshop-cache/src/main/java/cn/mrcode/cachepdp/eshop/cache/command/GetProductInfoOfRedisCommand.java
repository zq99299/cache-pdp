package cn.mrcode.cachepdp.eshop.cache.command;

import com.alibaba.fastjson.JSON;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

import cn.mrcode.cachepdp.eshop.cache.model.ProductInfo;
import redis.clients.jedis.JedisCluster;

/**
 * @author : zhuqiang
 * @date : 2019/6/23 15:17
 */
public class GetProductInfoOfRedisCommand extends HystrixCommand<ProductInfo> {
    private JedisCluster jedisCluster;
    private Long productId;

    public GetProductInfoOfRedisCommand(Long productId) {
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("GetProductInfoOfRedisCommand")));
        this.productId = productId;
    }

    @Override
    protected ProductInfo run() throws Exception {
        String key = "product_info_" + productId;
        String json = jedisCluster.get(key);
        return JSON.parseObject(json, ProductInfo.class);
    }

    @Override
    protected ProductInfo getFallback() {
        return null;
    }

    public JedisCluster getJedisCluster() {
        return jedisCluster;
    }

    public void setJedisCluster(JedisCluster jedisCluster) {
        this.jedisCluster = jedisCluster;
    }
}
