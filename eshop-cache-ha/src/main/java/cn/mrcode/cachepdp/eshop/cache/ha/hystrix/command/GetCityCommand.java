package cn.mrcode.cachepdp.eshop.cache.ha.hystrix.command;

import com.alibaba.fastjson.JSON;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;

import java.util.concurrent.TimeUnit;

import cn.mrcode.cachepdp.eshop.cache.ha.http.HttpClientUtils;
import cn.mrcode.cachepdp.eshop.cache.ha.model.ProductInfo;

/**
 * 基于信号量方式资源隔离
 *
 * @author : zhuqiang
 * @date : 2019/6/2 17:45
 */
public class GetCityCommand extends HystrixCommand<ProductInfo> {
    private Long productId;

    public GetCityCommand(Long productId) {
//        super(HystrixCommandGroupKey.Factory.asKey("GetProductCommandGroup"));
        // 线程组名
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("GetProductCommandGroup"))
                // 超时时间
                .andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
                        // 设置 4 秒超时，看是否有效果
                        .withExecutionTimeoutInMilliseconds(6000)
                        .withExecutionIsolationStrategy(HystrixCommandProperties.ExecutionIsolationStrategy.SEMAPHORE)
                        // 信号量最大请求数量设置
                        .withExecutionIsolationSemaphoreMaxConcurrentRequests(2)
                )

        );
        this.productId = productId;
    }

    @Override
    protected ProductInfo run() throws Exception {
        System.out.println(Thread.currentThread().getName());
        String url = "http://localhost:7000/getProduct?productId=" + productId;
        String response = HttpClientUtils.sendGetRequest(url);
        System.out.println("睡眠 5 秒，模拟");
        TimeUnit.SECONDS.sleep(5);
        return JSON.parseObject(response, ProductInfo.class);
    }
}
