package cn.mrcode.cachepdp.eshop.inventory.service;

import cn.mrcode.cachepdp.eshop.inventory.request.Request;

/**
 * 请求异步执行的 service
 *
 * @author : zhuqiang
 * @date : 2019/4/6 15:07
 */
public interface RequestAsyncProcessService {
    void process(Request request);
}
