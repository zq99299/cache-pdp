package cn.mrcode.cachepdp.eshop.cache.ha.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import cn.mrcode.cachepdp.eshop.cache.ha.http.HttpClientUtils;
import cn.mrcode.cachepdp.eshop.cache.ha.hystrix.command.GetCityCommand;
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

    /**
     * @param productIds 英文逗号分隔
     */
    @RequestMapping("/getProducts")
    public void getProduct(String productIds) {
        List<Long> pids = Arrays.stream(productIds.split(",")).map(Long::parseLong).collect(Collectors.toList());
        for (Long pid : pids) {
            GetProductCommand getProductCommand = new GetProductCommand(pid);
            getProductCommand.execute();
            System.out.println("pid " + pid + "；是否来自缓存：" + getProductCommand.isResponseFromCache());
        }
//        GetProductsCommand getProductsCommand = new GetProductsCommand(pids.toArray(new Long[pids.size()]));
        // 第一种获取数据模式
//        getProductsCommand.observe().subscribe(productInfo -> {
//            System.out.println(productInfo);
//        });


        // 第二种获取数据模式
        // 注意不要多次在同一个 command 上订阅
        // 否则报错 GetProductsCommand command executed multiple times - this is not permitted.
//        getProductsCommand.observe().subscribe(new Observer<ProductInfo>() {
//
//            @Override
//            public void onCompleted() {
//                System.out.println("Observer: onCompleted");
//            }
//
//            @Override
//            public void onError(Throwable e) {
//                System.out.println("Observer: onError:" + e);
//            }
//
//            @Override
//            public void onNext(ProductInfo productInfo) {
//                System.out.println("Observer: onNext:" + productInfo);
//            }
//        });
        // 同步调用方式
//        Iterator<ProductInfo> iterator = getProductsCommand.observe().toBlocking().getIterator();
//        while (iterator.hasNext()) {
//            System.out.println(iterator.next());
//        }
        System.out.println("方法已执行完成");
    }

    @RequestMapping("/semaphore/getProduct")
    public ProductInfo semaphoreGetProduct(Long productId) {
        GetCityCommand getCityCommand = new GetCityCommand(productId);
        System.out.println(Thread.currentThread().getName());
        ProductInfo productInfo = getCityCommand.execute();
        return productInfo;
    }
}
