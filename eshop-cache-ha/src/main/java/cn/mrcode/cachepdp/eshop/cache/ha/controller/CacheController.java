package cn.mrcode.cachepdp.eshop.cache.ha.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.mrcode.cachepdp.eshop.cache.ha.http.HttpClientUtils;
import cn.mrcode.cachepdp.eshop.cache.ha.hystrix.command.GetProductCommand;
import cn.mrcode.cachepdp.eshop.cache.ha.model.ProductInfo;

/**
 * ${todo}
 *
 * @author : zhuqiang
 * @date : 2019/6/1 22:27
 */
@RestController
public class CacheController {
    private Logger log = LoggerFactory.getLogger(getClass());

    @RequestMapping("/change/product")
    public String changeProduct(Long productId) {
        String url = "http://localhost:7000/getProduct?productId=" + productId;
        String response = HttpClientUtils.sendGetRequest(url);
        log.info(response);
        return "success";
    }

    @RequestMapping("/getProduct")
    public ProductInfo getProduct(Long productId) {
        GetProductCommand getProductCommand = new GetProductCommand(productId);
        // 同步执行
        ProductInfo productInfo = getProductCommand.execute();
        return productInfo;
    }
}
