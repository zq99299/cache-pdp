package cn.mrcode.cachepdp.eshop.storm;

import com.alibaba.fastjson.JSON;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.trident.util.LRUMap;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * ${todo}
 *
 * @author : zhuqiang
 * @date : 2019/5/22 23:29
 */
public class ProductCountBolt extends BaseRichBolt {
    private LRUMap<Long, Long> countMap = new LRUMap(100);
    private ZooKeeperSession zooKeeperSession = ZooKeeperSession.getInstance();
    private int taskId = -1;

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        taskId = context.getThisTaskId();
        // 启动一个线程，1 分钟计算一次
        topnStart();
        // 上报自己的节点 id 到列表中
        writeTaskPathToZk();
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
        final String lockPath = "hot-product-task-list-lock";
        final String taskListNode = "hot-product-task-list";
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
            proudcts.add(t.getKey());
        }
        final String taskNodePath = "hot-product-task-" + taskId;
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
}
