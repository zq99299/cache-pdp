package cn.mrcode.cachepdp.eshop.cache.ha;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolProperties;

import org.junit.Test;

import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

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
                                        .withCoreSize(10)
                                        // 设置线程池的最大大小，只有在设置 allowMaximumSizeToDivergeFromCoreSize 的时候才能生效
                                        .withMaximumSize(20)
                                        // 设置之后，其实 coreSize 就失效了
                                        .withAllowMaximumSizeToDivergeFromCoreSize(true)
                                        // 设置保持存活的时间，单位是分钟，默认是 1
                                        // 当线程池中线程空闲超过该时间之后，就会被销毁
                                        .withKeepAliveTimeMinutes(1)
                                // 配置等待线程个数；如果不配置该项，则没有等待，超过则拒绝
//                        .withMaxQueueSize(5)
                                // 由于 maxQueueSize 是初始化固定的，该配置项是动态调整最大等待数量的
                                // 可以热更新；规则：只能比 MaxQueueSize 小，
//                        .withQueueSizeRejectionThreshold(2)
                        )
                        .andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
                                .withExecutionTimeoutInMilliseconds(2000)
                                .withExecutionTimeoutEnabled(true)
                        ) // 修改为 2 秒超时
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
        int count = 23;
        CountDownLatch downLatch = new CountDownLatch(count);
        IntStream.range(0, count)
                .parallel()
                .mapToObj(item -> new Thread(() -> {
                    CommandLimit commandLimit = new CommandLimit();
                    String execute = commandLimit.execute();
                    System.out.println(Thread.currentThread().getName() + " " + item + " : " + execute + "  :  " + new Date());
                    downLatch.countDown();
                }))
                .forEach(item -> item.start());
//        for (int i = 0; i < count; i++) {
//            int finalI = i;
//            new Thread(() -> {
//                CommandLimit commandLimit = new CommandLimit();
//                String execute = commandLimit.execute();
//                System.out.println(Thread.currentThread().getName() + " " + finalI + " : " + execute + "  :  " + new Date());
//                downLatch.countDown();
//            }).start();
//        }
        downLatch.await();
    }

    @Test
    public void test2() throws InterruptedException {
        int count = 13;
        CountDownLatch downLatch = new CountDownLatch(count);
        for (int i = 0; i < count; i++) {
            int finalI = i;
            new Thread(() -> {
                CommandLimit commandLimit = new CommandLimit();
                String execute = commandLimit.execute();
                System.out.println(Thread.currentThread().hashCode() + " " + finalI + " : " + execute + "  :  " + new Date());
                downLatch.countDown();
            }).start();
        }
        downLatch.await();
        test3();
        // 休眠一分钟后，再次访问，查看线程池中线程
        TimeUnit.MINUTES.sleep(1);
        test3();
    }

    @Test
    public void test3() throws InterruptedException {
        int count = 13;
        CountDownLatch downLatch = new CountDownLatch(count);
        for (int i = 0; i < count; i++) {
            int finalI = i;
            new Thread(() -> {
                CommandLimit commandLimit = new CommandLimit();
                String execute = commandLimit.execute();
                System.out.println(Thread.currentThread().hashCode() + " " + finalI + " : " + execute + "  :  " + new Date());
                downLatch.countDown();
            }).start();
        }
        downLatch.await();
    }
}
