package cn.mrcode.cachepdp.eshop.inventory.dao;

/**
 * redis dao
 *
 * @author : zhuqiang
 * @date : 2019/4/3 21:22
 */
public interface RedisDAO {
    void set(String key, String value);

    String get(String key);

    void del(String buildKey);
}
