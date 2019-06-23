package cn.mrcode.cachepdp.eshop.cache.command;

import com.alibaba.fastjson.JSON;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

import cn.mrcode.cachepdp.eshop.cache.model.ShopInfo;
import redis.clients.jedis.JedisCluster;

/**
 * @author : zhuqiang
 * @date : 2019/6/23 15:18
 */
public class GetShopInfoOfRedisCommand extends HystrixCommand<ShopInfo> {
    private JedisCluster jedisCluster;
    private Long shopId;

    public GetShopInfoOfRedisCommand(Long shopId) {
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("GetShopInfoOfRedisCommand")));
        this.shopId = shopId;
    }

    @Override
    protected ShopInfo run() throws Exception {
        String key = "shop_info_" + shopId;
        String json = jedisCluster.get(key);
        return JSON.parseObject(json, ShopInfo.class);
    }

    public JedisCluster getJedisCluster() {
        return jedisCluster;
    }

    public void setJedisCluster(JedisCluster jedisCluster) {
        this.jedisCluster = jedisCluster;
    }


}
