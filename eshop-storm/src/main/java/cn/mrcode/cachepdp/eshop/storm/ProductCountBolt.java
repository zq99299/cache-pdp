package cn.mrcode.cachepdp.eshop.storm;

import com.alibaba.fastjson.JSON;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.trident.util.LRUMap;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.mrcode.cachepdp.eshop.storm.http.HttpClientUtils;

/**
 * ${todo}
 *
 * @author : zhuqiang
 * @date : 2019/5/22 23:29
 */
public class ProductCountBolt extends BaseRichBolt {
    private LRUMap<Long, Long> countMap = new LRUMap(100);
    private ZooKeeperSession zooKeeperSession;
    private int taskId = -1;

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        taskId = context.getThisTaskId();
        zooKeeperSession = ZooKeeperSession.getInstance();
        // 启动一个线程，1 分钟计算一次
        topnStart();
        // 上报自己的节点 id 到列表中
        writeTaskPathToZk();
        new HotProductFindThread(countMap).start();
    }

    private void topnStart() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int n = 3;
                Map.Entry<Long, Long>[] top = new Map.Entry[n];
                while (true) {
                    Arrays.fill(top, null);
                    Utils.sleep(6000);
                    for (Map.Entry<Long, Long> entry : countMap.entrySet()) {
                        long value = entry.getValue();
                        for (int i = 0; i < top.length; i++) {
                            Map.Entry<Long, Long> targetObj = top[i];
                            if (targetObj == null) {
                                top[i] = entry;
                                break;
                            }
                            long target = targetObj.getValue();
                            if (value > target) {
                                System.arraycopy(top, i, top, i + 1, n - (i + 1));
                                top[i] = entry;
                                break;
                            }
                        }
                    }
                    System.out.println(Thread.currentThread().getName() + "：" + Arrays.toString(top));
                    // 把结果接入到 zk 上
                    writeTopnToZk(top);
                }
            }
        }).start();
    }

    private void writeTaskPathToZk() {
        // 由于该操作是并发操作，需要通过分布式锁来写入
        final String lockPath = "/hot-product-task-list-lock";
        final String taskListNode = "/hot-product-task-list";
        zooKeeperSession.acquireDistributedLock(lockPath);
        String nodeData = zooKeeperSession.getNodeData(taskListNode);
        // 已经存在数据的话，把自己的 task id 追加到尾部
        if (nodeData != null && !"".equals(nodeData)) {
            nodeData += "," + taskId;
        } else {
            nodeData = taskId + "";
        }
        zooKeeperSession.setNodeData(taskListNode, nodeData);
        zooKeeperSession.releaseDistributedLock(lockPath);
    }

    private void writeTopnToZk(Map.Entry<Long, Long>[] topn) {
        List<Long> proudcts = new ArrayList<>();
        for (Map.Entry<Long, Long> t : topn) {
            if (t == null) {
                continue;
            }
            proudcts.add(t.getKey());
        }
        final String taskNodePath = "/hot-product-task-" + taskId;
        zooKeeperSession.setNodeData(taskNodePath, JSON.toJSONString(proudcts));
    }

    @Override
    public void execute(Tuple input) {
        Long productId = input.getLongByField("productId");
        Long count = countMap.get(productId);
        if (count == null) {
            count = 0L;
        }
        countMap.put(productId, ++count);
        System.out.println("商品 " + productId + ",次数 " + countMap.get(productId));
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {

    }

    /**
     * 热点商品感知
     */
    private static class HotProductFindThread extends Thread {
        private Logger logger = LoggerFactory.getLogger(getClass());
        private LRUMap<Long, Long> countMap;

        public HotProductFindThread(LRUMap<Long, Long> countMap) {
            this.countMap = countMap;
        }

        @Override
        public void run() {
            List<Map.Entry<Long, Long>> countList = new ArrayList<>();
            List<Long> hotPidList = new ArrayList<>();
            Set<Long> lastTimeHotPids = new HashSet<>();
            while (true) {
                Utils.sleep(5000);
                if (countMap.size() < 2) {
                    continue;
                }
                // 1. 全局排序
                countList.clear();
                hotPidList.clear();
                for (Map.Entry<Long, Long> entry : countMap.entrySet()) {
                    countList.add(entry);
                }
                Collections.sort(countList, new Comparator<Map.Entry<Long, Long>>() {
                    @Override
                    public int compare(Map.Entry<Long, Long> o1, Map.Entry<Long, Long> o2) {
                        return ~Long.compare(o1.getValue(), o2.getValue());
                    }
                });

                // 2.计算后 95% 商品平均访值,向上取整，至少 1 个商品
                // 1 * 0.95 = 0.95 向上取整为 1.0
                int avg95Count = (int) (countList.size() * 0.95);
                int avg95Total = 0;
                // 从列表尾部开始循环 avg95Count 次
                for (int i = countList.size() - 1; i >= countList.size() - avg95Count; i--) {
                    avg95Total += countList.get(i).getValue();
                }
                // 后百分之 95 商品的平均访问值
                int avg95Avg = avg95Total / avg95Count;
                logger.info("后 95% 商品数量 {} 个，平均访问值为 {}", avg95Count, avg95Avg);
                int threshold = 5; // 阈值

                // 3. 计算热点商品
                for (int i = 0; i < avg95Count; i++) {
                    Map.Entry<Long, Long> entry = countList.get(i);
                    if (entry.getValue() > avg95Avg * threshold) {
                        logger.info("发现一个热点商品：" + entry);
                        hotPidList.add(entry.getKey());
                        if (!lastTimeHotPids.contains(entry.getKey())) {
                            // 如果该商品已经是热点商品了，则不推送，新热点商品才推送
                            // 这里根据具体业务要求进行定制
                            // 推送热点商品信息到 所有 nginx 上
                            pushHotToNginx(entry.getKey());
                        }
                    }
                }
                logger.info("热点商品列表：" + hotPidList);

                // 4. 热点商品消失，通知 nginx 取消热点缓存
                if (lastTimeHotPids.size() > 0) {
                    // 上一次有热点商品
                    for (long lastTimeHotPid : lastTimeHotPids) {
                        // 但是不在这一次的热点中了，说明热点消失了
                        if (!hotPidList.contains(lastTimeHotPid)) {
                            logger.info("一个热点商品消失了：" + lastTimeHotPid);
                            String url = "http://eshop-cache03/cancel_hot?productId=" + lastTimeHotPid;
                            HttpClientUtils.sendGetRequest(url);
                        }
                    }
                }
                lastTimeHotPids.clear();
                for (Long pid : hotPidList) {
                    lastTimeHotPids.add(pid);
                }
            }
        }

        private void pushHotToNginx(Long pid) {
            // 降级策略推送到分发层 nginx
            String distributeNginxURL = "http://eshop-cache03/hot?productId=" + pid;
            HttpClientUtils.sendGetRequest(distributeNginxURL);

            // 获取商品信息
            String cacheServiceURL = "http://192.168.0.99:6002/getProductInfo?productId=" + pid;
            String response = HttpClientUtils.sendGetRequest(cacheServiceURL);

            try {
                response = URLEncoder.encode(response, "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            // 推送到应用层 nginx
            String[] appNginxURLs = new String[]{
                    "http://eshop-cache01/hot?productId=" + pid + "&productInfo=" + response,
                    "http://eshop-cache02/hot?productId=" + pid + "&productInfo=" + response
            };
            for (String appNginxURL : appNginxURLs) {
                HttpClientUtils.sendGetRequest(appNginxURL);
            }
        }

        public static void main(String[] args) throws UnsupportedEncodingException {
            System.out.println(Math.ceil(1 * 0.95));
        }
    }
}
