package cn.mrcode.cachepdp.eshop.inventory.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import cn.mrcode.cachepdp.eshop.inventory.dao.RedisDAO;
import redis.clients.jedis.JedisCluster;

/**
 * ${todo}
 *
 * @author : zhuqiang
 * @date : 2019/4/3 21:23
 */
@Repository
public class RedisDAOImpl implements RedisDAO {
    @Autowired
    private JedisCluster jedisCluster;

    @Override
    public void set(String key, String value) {
        jedisCluster.set(key, value);
    }

    @Override
    public String get(String key) {
        return jedisCluster.get(key);
    }

    @Override
    public void del(String buildKey) {
        jedisCluster.del(buildKey);
    }
}
