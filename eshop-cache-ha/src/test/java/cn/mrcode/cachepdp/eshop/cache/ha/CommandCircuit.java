package cn.mrcode.cachepdp.eshop.cache.ha;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;

/**
 * ${todo}
 *
 * @author : zhuqiang
 * @date : 2019/6/3 23:07
 */
public class CommandCircuit extends HystrixCommand<String> {

    private final boolean throwException;

    public CommandCircuit(boolean throwException) {
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("test"))
                .andCommandPropertiesDefaults(
                        HystrixCommandProperties.Setter()
                                // 手动控制断路器是否启用
                                .withCircuitBreakerEnabled(true)
                                // 10 秒时间窗口流量达到 10 个；默认是 20
                                .withCircuitBreakerRequestVolumeThreshold(10)
                                // 当异常占比超过 50% ；默认值是 50
                                .withCircuitBreakerErrorThresholdPercentage(50)
                                // 断路器打开之后，后续请求都会被拒绝并走降级机制，打开 3 秒后，变成半开状态
                                .withCircuitBreakerSleepWindowInMilliseconds(3000)
                )
        );
        this.throwException = throwException;
    }

    @Override
    protected String run() {
        if (throwException) {
            throw new RuntimeException("failure from CommandThatFailsFast");
        } else {
            return "success";
        }
    }

    @Override
    protected String getFallback() {
        return "降级机制";
    }
}
