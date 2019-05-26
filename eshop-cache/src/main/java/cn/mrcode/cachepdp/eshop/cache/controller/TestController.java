package cn.mrcode.cachepdp.eshop.cache.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ${todo}
 *
 * @author : zhuqiang
 * @date : 2019/5/26 22:29
 */
@RestController
@RequestMapping("/test")
public class TestController {
    @GetMapping
    public void hot(Long productId, String productInfo) {
        System.out.println(productId + "  :  " + productInfo);
    }
}
