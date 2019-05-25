package cn.mrcode.cachepdp.eshop.storm;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.trident.util.LRUMap;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.utils.Utils;

import java.util.Arrays;
import java.util.Map;

/**
 * ${todo}
 *
 * @author : zhuqiang
 * @date : 2019/5/22 23:29
 */
public class ProductCountBolt extends BaseRichBolt {
    private LRUMap<Long, Long> countMap = new LRUMap(100);

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        // 启动一个线程，1 分钟计算一次
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
                }
            }
        }).start();
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
