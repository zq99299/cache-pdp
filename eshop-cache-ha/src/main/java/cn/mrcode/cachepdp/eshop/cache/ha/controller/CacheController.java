package cn.mrcode.cachepdp.eshop.cache.ha.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import cn.mrcode.cachepdp.eshop.cache.ha.http.HttpClientUtils;
import cn.mrcode.cachepdp.eshop.cache.ha.hystrix.command.CollapserGetProductCommand;
import cn.mrcode.cachepdp.eshop.cache.ha.hystrix.command.GetCityCommand;
import cn.mrcode.cachepdp.eshop.cache.ha.hystrix.command.GetProductCommand2;
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
        GetProductCommand2 getProductCommand = new GetProductCommand2(productId);
        // 同步执行
        ProductInfo productInfo = getProductCommand.execute();
        return productInfo;
    }

    /**
     * @param productIds 英文逗号分隔
     */
    @RequestMapping("/getProducts")
    public void getProduct(String productIds) throws ExecutionException, InterruptedException {
        List<Long> pids = Arrays.stream(productIds.split(",")).map(Long::parseLong).collect(Collectors.toList());

        // 在批量获取商品接口中来使用请求合并
//        for (Long pid : pids) {
//            CollapserGetProductCommand getProductCommand = new CollapserGetProductCommand(pid);
//            Future<ProductInfo> queue = getProductCommand.queue();
//            System.out.println("请求结果：" + queue.get() + ": 是否只请求合并：" + queue.isCancelled());
//        }
        // 不要使用上面的调用方式，因为这样做就相当于是同步调用了，一个请求回来之后才能继续下一个
        List<Future<ProductInfo>> results = pids.stream()
                .map(pid -> {
                    CollapserGetProductCommand getProductCommand = new CollapserGetProductCommand(pid);
                    return getProductCommand.queue();
                })
                .collect(Collectors.toList());
        for (Future<ProductInfo> result : results) {
            System.out.println("请求结果：" + result.get());
        }
//        for (Long pid : pids) {
//            GetProductCommand getProductCommand = new GetProductCommand(pid);
//            getProductCommand.execute();
//            System.out.println("pid " + pid + "；是否来自缓存：" + getProductCommand.isResponseFromCache());
//        }
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
