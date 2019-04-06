package cn.mrcode.cachepdp.eshop.inventory.request;

import com.alibaba.fastjson.JSON;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 处理请求的线程
 *
 * @author : zhuqiang
 * @date : 2019/4/3 22:38
 */
public class RequestProcessorThread implements Callable<Boolean> {
    private ArrayBlockingQueue<Request> queue;
    /**
     * k: 商品 id v：请求标志： true : 有更新请求
     */
    private Map<Integer, Boolean> flagMap = new ConcurrentHashMap<>();
    private Logger log = LoggerFactory.getLogger(getClass());

    public RequestProcessorThread(ArrayBlockingQueue<Request> queue) {
        this.queue = queue;
    }

    @Override
    public Boolean call() throws Exception {
        try {
            while (true) {
                Request request = queue.take();
                log.info("处理请求：{}", JSON.toJSONString(request));
                // 非强制刷新请求的话，就是一个正常的读请求
                if (!request.isForceRfresh()) {
                    // 如果是一个更新数据库请求
                    if (request instanceof ProductInventoryDBUpdateRequest) {
                        flagMap.put(request.getProductId(), true);
                        log.info("写请求：{}", JSON.toJSONString(request));
                    } else if (request instanceof ProductInventoryCacheRefreshRequest) {
                        log.info("读请求：{}", JSON.toJSONString(request));
                        Boolean flag = flagMap.get(request.getProductId());
                        if (flag == null) {
                            log.info("flag 为 null：{}", JSON.toJSONString(request));
                            flagMap.put(request.getProductId(), false);
                        }
                        // 已经有过读或写的请求 并且前面已经有一个写请求了
                        if (flag != null && flag) {
                            // 读取请求把，写请求标志冲掉
                            // 本次读会正常的执行，组成 1+1 （1 写 1 读）
                            // 后续的正常读请求会被过滤掉
                            flagMap.put(request.getProductId(), false);
                            log.info("1+1 达成，1 写 1 读：{}", JSON.toJSONString(request));
                        }
                        // 如果是读请求，直接返回，等待写完成即可
                        else if (flag != null && !flag) {
                            log.info("已有 1+1 ，放弃处理该次请求：{}", JSON.toJSONString(request));
                            continue;
                        }
                    }
                }
                request.process();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }
}
