package cn.mrcode.cachepdp.eshop.cache.ha;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixInvokableInfo;
import com.netflix.hystrix.HystrixRequestLog;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;

import org.junit.Test;

import java.util.Collection;
import java.util.concurrent.Future;

/**
 * ${todo}
 *
 * @author : zhuqiang
 * @date : 2019/6/11 21:27
 */
public class CommandCollapserGetValueForKeyTest {
    @Test
    public void testCollapser() throws Exception {
        // 创建一个上下文
        HystrixRequestContext context = HystrixRequestContext.initializeContext();

        try {
            // 这里不能使用多线程来并发请求，因为请求合并技术依赖于上下文
            // 而这里初始化的是一个 HystrixRequestContext 上下文，也就是线程级别的
            // 如果使用多线程，那么必然会报错
            // 而且请求合并默认合并范围也是单个线程范围
            Future<String> f1 = new CommandCollapserGetValueForKey(1).queue();
            Future<String> f2 = new CommandCollapserGetValueForKey(2).queue();
            Future<String> f3 = new CommandCollapserGetValueForKey(3).queue();
            Future<String> f4 = new CommandCollapserGetValueForKey(4).queue();

            System.out.println(f1.get());
            System.out.println(f2.get());
            System.out.println(f3.get());
            System.out.println(f4.get());

//            assert that the batch command 'GetValueForKey' was in fact
//            executed and that it executed only once
            HystrixRequestLog currentRequest = HystrixRequestLog.getCurrentRequest();
            Collection<HystrixInvokableInfo<?>> allExecutedCommands = currentRequest.getAllExecutedCommands();
            System.out.println("当前线程中请求实际发起了几次：" + allExecutedCommands.size());
            HystrixCommand<?>[] hystrixCommands = allExecutedCommands.toArray(new HystrixCommand<?>[allExecutedCommands.size()]);
            HystrixCommand<?> hystrixCommand = hystrixCommands[0];
            System.out.println("其中第一个 command 的名称：" + hystrixCommand.getCommandKey());
            System.out.println("command 执行事件" + hystrixCommand.getExecutionEvents());
        } finally {
            context.shutdown();
        }
    }
}