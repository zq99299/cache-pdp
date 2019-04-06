package cn.mrcode.cachepdp.eshop.inventory.service;

import cn.mrcode.cachepdp.eshop.inventory.model.User;

/**
 * ${todo}
 *
 * @author : zhuqiang
 * @date : 2019/4/1 22:05
 */
public interface UserService {
    User getUserInfo();

    /**
     * 查询缓存的信息
     */
    User getCachedUserInfo();
}
