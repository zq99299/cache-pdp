package cn.mrcode.cachepdp.eshop.inventory.request;

/**
 * 请求对象接口
 *
 * @author : zhuqiang
 * @date : 2019/4/3 22:34
 */
public interface Request {
    void process();

    Integer getProductId();

    boolean isForceRfresh();
}
