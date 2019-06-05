package cn.mrcode.cachepdp.eshop.cache.ha;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolProperties;

import org.junit.Test;

import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * ${todo}
 *
 * @author : zhuqiang
 * @date : 2019/6/5 22:15
 */
public class CommandLimit extends HystrixCommand<String> {
    public CommandLimit() {
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("test-group"))
                .andThreadPoolPropertiesDefaults(HystrixThreadPoolProperties.Setter()
                        // 配置线程池大小，同时并发能力个数
                        .withCoreSize(2)
                        // 配置等待线程个数；如果不配置该项，则没有等待，超过则拒绝
                        .withMaxQueueSize(5)
                        // 由于 maxQueueSize 是初始化固定的，该配置项是动态调整最大等待数量的
                        // 可以热更新；规则：只能比 MaxQueueSize 小，
                        .withQueueSizeRejectionThreshold(2)
                )
                .andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
                        .withExecutionTimeoutInMilliseconds(2000)) // 修改为 2 秒超时
        );
    }

    @Override
    protected String run() throws Exception {
//        TimeUnit.MILLISECONDS.sleep(800);
        TimeUnit.MILLISECONDS.sleep(1000);
        return "success";
    }

    @Override
    protected String getFallback() {
        return "降级";
    }

    @Test
    public void test() throws InterruptedException {
        int count = 13;
        CountDownLatch downLatch = new CountDownLatch(count);
        for (int i = 0; i < count; i++) {
            int finalI = i;
            new Thread(() -> {
                CommandLimit commandLimit = new CommandLimit();
                String execute = commandLimit.execute();
                System.out.println(Thread.currentThread().getName() + " " + finalI + " : " + execute + "  :  " + new Date());
                downLatch.countDown();
            }).start();
        }
        downLatch.await();
    }
}
