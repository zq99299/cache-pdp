package cn.mrcode.cachepdp.eshop.inventory.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ArrayBlockingQueue;

import cn.mrcode.cachepdp.eshop.inventory.request.Request;
import cn.mrcode.cachepdp.eshop.inventory.request.RequestQueue;
import cn.mrcode.cachepdp.eshop.inventory.service.RequestAsyncProcessService;

/**
 * ${todo}
 *
 * @author : zhuqiang
 * @date : 2019/4/6 15:08
 */
@Service
public class RequestAsyncProcessServiceImpl implements RequestAsyncProcessService {
    @Autowired
    private RequestQueue requestQueue;
    private Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public void process(Request request) {
        try {
            // 1. 根据商品 id 路由到具体的队列
            ArrayBlockingQueue<Request> queue = getRoutingQueue(request.getProductId());
            // 2. 放入队列
            queue.put(request);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private ArrayBlockingQueue<Request> getRoutingQueue(Integer productId) {
        // 先获取 productId 的 hash 值
        String key = String.valueOf(productId);
        int h;
        int hash = (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);

        // 对hash值取模，将hash值路由到指定的内存队列中，比如内存队列大小8
        // 用内存队列的数量对hash值取模之后，结果一定是在0~7之间
        // 所以任何一个商品id都会被固定路由到同样的一个内存队列中去的
        int index = (requestQueue.queueSize() - 1) & hash;
        log.info("路由信息：key={},商品 ID ={},队列 index={}", key, productId, index);
        return requestQueue.getQueue(index);
    }
}
