package cn.mrcode.cachepdp.eshop.storm;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.trident.util.LRUMap;
import org.apache.storm.tuple.Tuple;

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
