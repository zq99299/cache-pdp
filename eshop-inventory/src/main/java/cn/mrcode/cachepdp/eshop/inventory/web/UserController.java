package cn.mrcode.cachepdp.eshop.inventory.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import cn.mrcode.cachepdp.eshop.inventory.model.User;
import cn.mrcode.cachepdp.eshop.inventory.service.UserService;

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
    @ResponseBody
    public User getUserInfo() {
        User user = userService.getUserInfo();
        return user;
    }

    @RequestMapping("/getCachedUserInfo")
    @ResponseBody
    public User getCachedUserInfo() {
        User user = userService.getCachedUserInfo();
        return user;
    }
}
