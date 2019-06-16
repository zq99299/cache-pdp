package cn.mrcode.cachepdp.eshop.cache.ha;

import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixObservableCommand;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * ${todo}
 *
 * @author : zhuqiang
 * @date : 2019/6/3 23:07
 */
public class CommandThatFailsFast2 extends HystrixObservableCommand<Integer> {

    private int lastSeen = 0;

    public CommandThatFailsFast2() {
        super(HystrixCommandGroupKey.Factory.asKey("ExampleGroup"));
    }

    @Override
    protected Observable<Integer> construct() {
        return Observable.just(1, 2, 3)
                .concatWith(Observable.error(new RuntimeException("forced error")))
                .doOnNext(t1 -> lastSeen = t1)
                .subscribeOn(Schedulers.computation());
    }

    @Override
    protected Observable<Integer> resumeWithFallback() {
        if (lastSeen < 4) {
            return Observable.range(lastSeen + 1, 4 - lastSeen);
        } else {
            return Observable.empty();
        }
    }
}
