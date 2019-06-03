package cn.mrcode.cachepdp.eshop.cache.ha;

import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * ${todo}
 *
 * @author : zhuqiang
 * @date : 2019/6/3 21:52
 */
public class CommandUsingRequestCacheTest {
    @Test
    public void testWithoutCacheHits() {
        HystrixRequestContext context = HystrixRequestContext.initializeContext();
        try {
            assertTrue(new CommandUsingRequestCache(2).execute());
            assertTrue(new CommandUsingRequestCache(2).execute());
            assertFalse(new CommandUsingRequestCache(1).execute());
            assertTrue(new CommandUsingRequestCache(2).execute());
            assertTrue(new CommandUsingRequestCache(0).execute());
            assertTrue(new CommandUsingRequestCache(58672).execute());
        } finally {
            context.shutdown();
        }
    }

    @Test
    public void testWithCacheHits() {
        HystrixRequestContext context = HystrixRequestContext.initializeContext();
        try {
            CommandUsingRequestCache command2a = new CommandUsingRequestCache(2);
            CommandUsingRequestCache command2b = new CommandUsingRequestCache(2);

            assertTrue(command2a.execute());
            // 第一次执行结果，所以不应该来自缓存
            assertFalse(command2a.isResponseFromCache());
//            HystrixRequestCache.getInstance(command2a.getCommandKey(),
//                    HystrixConcurrencyStrategyDefault.getInstance()).clear(command2a.getCacheKey());
            assertTrue(command2b.execute());
            // 这是第二次执行结果，应该来自缓存
            assertTrue(command2b.isResponseFromCache());
        } finally {
            // 关闭上下文
            context.shutdown();
        }

        // 开始一个新的请求上下文
        context = HystrixRequestContext.initializeContext();
        try {
            CommandUsingRequestCache command3b = new CommandUsingRequestCache(2);
            assertTrue(command3b.execute());
            // 当前的 command 是一个新的请求上下文
            // 所以也不应该来自缓存
            assertFalse(command3b.isResponseFromCache());
        } finally {
            context.shutdown();
        }
    }
}