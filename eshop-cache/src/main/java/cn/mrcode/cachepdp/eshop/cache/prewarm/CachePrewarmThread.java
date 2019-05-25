package cn.mrcode.cachepdp.eshop.cache.prewarm;

import com.alibaba.fastjson.JSON;

import java.util.List;

import cn.mrcode.cachepdp.eshop.cache.SpringContextUtil;
import cn.mrcode.cachepdp.eshop.cache.ZooKeeperSession;
import cn.mrcode.cachepdp.eshop.cache.model.ProductInfo;
import cn.mrcode.cachepdp.eshop.cache.service.CacheService;

/**
 * ${todo}
 *
 * @author : zhuqiang
 * @date : 2019/5/25 16:02
 */
public class CachePrewarmThread extends Thread {
    @Override
    public void run() {
        // 1. 获取 task id 列表
        ZooKeeperSession zk = ZooKeeperSession.getInstance();
        final String taskListNode = "/hot-product-task-list";
        String taskListNdeData = zk.getNodeData(taskListNode);
        if (taskListNode == null || "".equals(taskListNdeData)) {
            System.err.println("task list 为空");
            return;
        }

        CacheService cacheService = SpringContextUtil.getWebApplicationContext().getBean(CacheService.class);

        String[] taskList = taskListNdeData.split(",");
        for (String taskId : taskList) {
            final String taskNodeLockPath = "/hot-product-task-lock-" + taskId;
            // 尝试获取该节点的锁，如果获取失败，说明被其他服务预热了
            if (!zk.acquireFastFailDistributedLock(taskNodeLockPath)) {
                continue;
            }

            // 获取 检查预热状态
            final String taskNodePrewarmStatePath = "/hot-product-task-prewarm-state" + taskId;
            String taskNodePrewarmState = zk.getNodeData(taskNodePrewarmStatePath);
            // 已经被预热过了
            if (taskNodePrewarmState != null && !"".equals(taskNodePrewarmState)) {
                zk.releaseDistributedLock(taskNodeLockPath);
                continue;
            }

            // 还未被预热过，读取 topn 列表，并从数据库中获取商品信息，存入缓存中
            final String taskNodePath = "/hot-product-task-" + taskId;
            String nodeData = zk.getNodeData(taskNodePath);
            if (nodeData == null && "".equals(nodeData)) {
                // 如果没有数据则不处理
                zk.releaseDistributedLock(taskNodeLockPath);
                continue;
            }

            List<Long> pids = JSON.parseArray(nodeData, Long.class);

            // 假设这里是从数据库中获取的数据
            pids.forEach(pid -> {
                ProductInfo productInfo = getProduct(pid);
                System.out.println("预热缓存信息：" + productInfo);
                cacheService.saveProductInfo2LocalCache(productInfo);
                cacheService.saveProductInfo2ReidsCache(productInfo);
            });

            // 修改预热状态
            zk.setNodeData(taskNodePrewarmStatePath, "success");
            // 释放该 task 节点的锁
            zk.releaseDistributedLock(taskNodeLockPath);
        }
    }

    private ProductInfo getProduct(Long pid) {
        String productInfoJSON = "{\"id\": " + pid + ", \"name\": \"iphone7手机\", \"price\": 5599, \"pictureList\":\"a.jpg,b.jpg\", \"specification\": \"iphone7的规格\", \"service\": \"iphone7的售后服务\", \"color\": \"红色,白色,黑色\", \"size\": \"5.5\", \"shopId\": 1," +
                "\"modifyTime\":\"2019-05-13 22:00:00\"}";
        return JSON.parseObject(productInfoJSON, ProductInfo.class);
    }
}
