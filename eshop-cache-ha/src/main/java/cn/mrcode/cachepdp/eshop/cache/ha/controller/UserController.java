package cn.mrcode.cachepdp.eshop.cache.ha.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.mrcode.cachepdp.eshop.cache.ha.model.User;
import cn.mrcode.cachepdp.eshop.cache.ha.service.UserService;


/**
 * ${todo}
 *
 * @author : zhuqiang
 * @date : 2019/4/1 22:11
 */
@RestController
public class UserController {
    @Autowired
    private UserService userService;

    @RequestMapping("/getUserInfo")
    public User getUserInfo() {
        User user = userService.getUserInfo();
        return user;
    }
}
