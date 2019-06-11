package cn.mrcode.cachepdp.eshop.cache.ha.hystrix.command;

import com.alibaba.fastjson.JSON;
import com.netflix.hystrix.HystrixCollapser;
import com.netflix.hystrix.HystrixCollapserKey;
import com.netflix.hystrix.HystrixCollapserProperties;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import cn.mrcode.cachepdp.eshop.cache.ha.http.HttpClientUtils;
import cn.mrcode.cachepdp.eshop.cache.ha.model.ProductInfo;

/**
 * @author : zhuqiang
 * @date : 2019/6/11 21:55
 */
public class CollapserGetProductCommand extends HystrixCollapser<List<ProductInfo>, ProductInfo, Long> {
    private final Long productId;

    public CollapserGetProductCommand(Long productId) {
        super(Setter.withCollapserKey(HystrixCollapserKey.Factory.asKey("CollapserGetProductCommand"))
                .andCollapserPropertiesDefaults(HystrixCollapserProperties.Setter()
                        // 在 TimerDelayInMilliseconds 内最多允许多少个 request 被合并
                        // 默认是无限大，该参数一般不使用，而是使用时间来触发合并请求提交
                        .withMaxRequestsInBatch(10)
                        // 时间窗口：合并请求需要等待多久
                        // 默认是 10ms ，
                        .withTimerDelayInMilliseconds(20)
                )
        );
        this.productId = productId;
    }

    @Override
    public Long getRequestArgument() {
        return productId;
    }

    @Override
    protected HystrixCommand<List<ProductInfo>> createCommand(Collection<CollapsedRequest<ProductInfo, Long>> collapsedRequests) {
        // 实现注意:要快。（<1ms)，否则会阻止计时器执行后续批次。除了构造命令并返回它之外，不要执行任何处理。
        return new BatchCommand(collapsedRequests);
    }

    @Override
    protected void mapResponseToRequests(List<ProductInfo> batchResponse, Collection<CollapsedRequest<ProductInfo, Long>> collapsedRequests) {
        int count = 0;
        for (CollapsedRequest<ProductInfo, Long> collapsedRequest : collapsedRequests) {
            collapsedRequest.setResponse(batchResponse.get(count++));
        }
        System.out.println("映射数量：" + collapsedRequests.size());
    }

    public static class BatchCommand extends HystrixCommand<List<ProductInfo>> {
        private Collection<CollapsedRequest<ProductInfo, Long>> requests;

        public BatchCommand(Collection<CollapsedRequest<ProductInfo, Long>> requests) {
            super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("ExampleGroup"))
                    .andCommandKey(HystrixCommandKey.Factory.asKey("GetValueForKy"))

            );
            this.requests = requests;
            System.out.println("此次请求大小：" + requests.size());
        }

        @Override
        protected List<ProductInfo> run() throws Exception {
            // 从当前合并的多个请求中，按顺序拼接请求的 pid
            String productIdsStr = requests.stream()
                    .map(item -> item.getArgument())
                    .map(item -> String.valueOf(item))
                    .collect(Collectors.joining(","));
            System.out.println("执行批量接口请求:" + productIdsStr);
            String url = "http://localhost:7000/getProducts?productIdsStr=" + productIdsStr;
            String response = HttpClientUtils.sendGetRequest(url);
            return JSON.parseArray(response, String.class)
                    .stream()
                    .map(item -> JSON.parseObject(item, ProductInfo.class))
                    .collect(Collectors.toList());
        }
    }
}
