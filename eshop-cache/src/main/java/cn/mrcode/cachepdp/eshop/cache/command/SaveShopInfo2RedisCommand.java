package cn.mrcode.cachepdp.eshop.cache.command;

import com.alibaba.fastjson.JSONObject;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

import cn.mrcode.cachepdp.eshop.cache.model.ShopInfo;
import redis.clients.jedis.JedisCluster;

/**
 * @author : zhuqiang
 * @date : 2019/6/23 15:14
 */
public class SaveShopInfo2RedisCommand extends HystrixCommand<Boolean> {
    private JedisCluster jedisCluster;
    private final ShopInfo shopInfo;

    public SaveShopInfo2RedisCommand(ShopInfo shopInfo) {
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("SaveShopInfo2RedisCommand")));
        this.shopInfo = shopInfo;
    }

    @Override
    protected Boolean run() throws Exception {
        String key = "shop_info_" + shopInfo.getId();
        jedisCluster.set(key, JSONObject.toJSONString(shopInfo));
        return true;
    }

    public JedisCluster getJedisCluster() {
        return jedisCluster;
    }

    public void setJedisCluster(JedisCluster jedisCluster) {
        this.jedisCluster = jedisCluster;
    }
}
