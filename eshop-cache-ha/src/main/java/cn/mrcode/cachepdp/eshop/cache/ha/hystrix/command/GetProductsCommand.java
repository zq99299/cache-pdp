package cn.mrcode.cachepdp.eshop.cache.ha.hystrix.command;

import com.alibaba.fastjson.JSON;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixObservableCommand;

import cn.mrcode.cachepdp.eshop.cache.ha.http.HttpClientUtils;
import cn.mrcode.cachepdp.eshop.cache.ha.model.ProductInfo;
import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * ${todo}
 *
 * @author : zhuqiang
 * @date : 2019/6/2 15:41
 */
public class GetProductsCommand extends HystrixObservableCommand {
    private Long[] pids;

    public GetProductsCommand(Long[] pids) {
        super(HystrixCommandGroupKey.Factory.asKey("GetProductCommandGroup"));
        this.pids = pids;
    }

    @Override
    protected Observable construct() {
        // create OnSubscribe 方法已经过时
        // 文档说改为了 unsafeCreate 方法
        return Observable.unsafeCreate((Observable.OnSubscribe<ProductInfo>) onSubscribe -> {
//            for (Long pid : pids) {
//                String url = "http://localhost:7000/getProduct?productId=" + pid;
//                String response = HttpClientUtils.sendGetRequest(url);
//                onSubscribe.onNext(JSON.parseObject(response, ProductInfo.class));
//            }
//            onSubscribe.onCompleted();
            try {
                if (!onSubscribe.isUnsubscribed()) {
                    for (Long pid : pids) {
                        String url = "http://localhost:7000/getProduct?productId=" + pid;
                        String response = HttpClientUtils.sendGetRequest(url);
                        onSubscribe.onNext(JSON.parseObject(response, ProductInfo.class));
                    }
                    onSubscribe.onCompleted();
                }
            } catch (Exception e) {
                onSubscribe.onError(e);
            }
        }).subscribeOn(Schedulers.io());
    }
}
