package cn.mrcode.cachepdp.eshop.inventory.service.impl;

import com.alibaba.fastjson.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.mrcode.cachepdp.eshop.inventory.dao.RedisDAO;
import cn.mrcode.cachepdp.eshop.inventory.mapper.UserMapper;
import cn.mrcode.cachepdp.eshop.inventory.model.User;
import cn.mrcode.cachepdp.eshop.inventory.service.UserService;

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
    @Autowired
    private RedisDAO redisDAO;

    @Override
    public User getUserInfo() {
        return userMapper.findUserInfo();
    }

    @Override
    public User getCachedUserInfo() {
        redisDAO.set("cached_user", "{\"name\": \"zhangsan\", \"age\": 25}");
        String json = redisDAO.get("cached_user");
        JSONObject jsonObject = JSONObject.parseObject(json);

        User user = new User();
        user.setName(jsonObject.getString("name"));
        user.setAge(jsonObject.getInteger("age"));

        return user;
    }
}
