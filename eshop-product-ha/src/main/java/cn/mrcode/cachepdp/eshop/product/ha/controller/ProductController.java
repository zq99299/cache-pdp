package cn.mrcode.cachepdp.eshop.product.ha.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ${todo}
 *
 * @author : zhuqiang
 * @date : 2019/6/1 22:27
 */
@RestController
public class ProductController {
    private Logger log = LoggerFactory.getLogger(getClass());

    @RequestMapping("/getProduct")
    public String getProduct(Long productId) {
        String productInfoJSON = "{\"id\": " + productId + ", \"name\": \"iphone7手机\", \"price\": 5599, \"pictureList\":\"a.jpg,b.jpg\", \"specification\": \"iphone7的规格\", \"service\": \"iphone7的售后服务\", \"color\": \"红色,白色,黑色\", \"size\": \"5.5\", \"shopId\": 1," +
                "\"modifyTime\":\"2019-05-13 22:00:00\"}";
        return productInfoJSON;
    }
}
