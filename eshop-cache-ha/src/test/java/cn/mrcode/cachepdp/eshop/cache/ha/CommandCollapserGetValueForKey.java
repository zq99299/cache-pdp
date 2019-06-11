package cn.mrcode.cachepdp.eshop.cache.ha;

import com.netflix.hystrix.HystrixCollapser;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @param <BatchReturnType>     一个合并请求 HystrixCommand 执行返回的结果
 * @param <ResponseType>        单个请求返回的结果
 * @param <RequestArgumentType> 单个 HystrixCommand 请求参数
 * @author : zhuqiang
 * @date : 2019/6/11 21:04
 */
public class CommandCollapserGetValueForKey extends HystrixCollapser<List<String>, String, Integer> {
    private final Integer key;

    public CommandCollapserGetValueForKey(Integer key) {
        this.key = key;
    }

    /**
     * 单个 command 请求参数
     */
    @Override
    public Integer getRequestArgument() {
        return key;
    }

    /**
     * 聚合多个命令由框架完成，这里只需要创建我们的 batchCommand 即可
     *
     * @param collapsedRequests 这个是多个请求的参数列表
     */
    @Override
    protected HystrixCommand<List<String>> createCommand(Collection<CollapsedRequest<String, Integer>> collapsedRequests) {
        return new BatchCommand(collapsedRequests);
    }

    /**
     * 将返回的数据对请求进行映射，外部的单个请求才能获取到对应的结果
     */
    @Override
    protected void mapResponseToRequests(List<String> batchResponse, Collection<CollapsedRequest<String, Integer>> collapsedRequests) {
        int count = 0;
        for (CollapsedRequest<String, Integer> collapsedRequest : collapsedRequests) {
            // 把请求回来的结果再分发到对应的请求中去
            collapsedRequest.setResponse(batchResponse.get(count++));
        }
    }

    /**
     * 发起批量请求的 command
     */
    private static final class BatchCommand extends HystrixCommand<List<String>> {
        private final Collection<CollapsedRequest<String, Integer>> requests;

        public BatchCommand(Collection<CollapsedRequest<String, Integer>> requests) {
            super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("ExampleGroup"))
                    .andCommandKey(HystrixCommandKey.Factory.asKey("GetValueForKy"))
            );
            this.requests = requests;
        }

        @Override
        protected List<String> run() throws Exception {
            List<String> response = new ArrayList<>();
            // 这里模拟拿到这一组的请求参数去请求接口，然后返回数据
            for (CollapsedRequest<String, Integer> request : requests) {
                response.add("ValueForKey：" + request.getArgument());
            }
            System.out.println("请求合并-BatchCommand 执行");
            return response;
        }
    }
}
