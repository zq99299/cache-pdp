package cn.mrcode.cachepdp.eshop.product.ha.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.mrcode.cachepdp.eshop.product.ha.mappter.UserMapper;
import cn.mrcode.cachepdp.eshop.product.ha.model.User;
import cn.mrcode.cachepdp.eshop.product.ha.service.UserService;

/**
 * ${todo}
 *
 * @author : zhuqiang
 * @date : 2019/4/1 22:11
 */
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;

    @Override
    public User getUserInfo() {
        return userMapper.findUserInfo();
    }
}
