package cn.mrcode.cachepdp.eshop.cache.ha.hystrix.command;

import com.alibaba.fastjson.JSON;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolProperties;

import java.util.concurrent.TimeUnit;

import cn.mrcode.cachepdp.eshop.cache.ha.http.HttpClientUtils;
import cn.mrcode.cachepdp.eshop.cache.ha.model.ProductInfo;

/**
 * ${todo}
 *
 * @author : zhuqiang
 * @date : 2019/6/1 23:45
 */
public class GetProductCommand extends HystrixCommand<ProductInfo> {
    private Long productId;

    public GetProductCommand(Long productId) {
//        super(HystrixCommandGroupKey.Factory.asKey("GetProductCommandGroup"));
        // 线程组名
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("GetProductCommandGroup"))
                // 超时时间
                .andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
                        .withExecutionTimeoutInMilliseconds(6000)
                        .withExecutionIsolationStrategy(HystrixCommandProperties.ExecutionIsolationStrategy.SEMAPHORE)
                )
                .andThreadPoolPropertiesDefaults(HystrixThreadPoolProperties.Setter()
                        // 线程池大小，最多有多少个线程同时并发
                        .withCoreSize(2)
                        // 排队，默认为 -1 ，假设 10 个请求，2 个执行，2 个排队，那么其他 6 个将直接返回错误
                        .withMaxQueueSize(2)
                )

        );
        this.productId = productId;
    }

    @Override
    protected String getCacheKey() {
        return String.valueOf(productId);
    }

    @Override
    protected ProductInfo run() throws Exception {
        String url = "http://localhost:7000/getProduct?productId=" + productId;
        String response = HttpClientUtils.sendGetRequest(url);
        System.out.println("睡眠 5 秒，模拟");
        TimeUnit.SECONDS.sleep(5);
        return JSON.parseObject(response, ProductInfo.class);
    }
}
