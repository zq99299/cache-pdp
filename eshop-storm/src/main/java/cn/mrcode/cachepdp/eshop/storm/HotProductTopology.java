package cn.mrcode.cachepdp.eshop.storm;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;

import java.util.concurrent.TimeUnit;

/**
 * ${todo}
 *
 * @author : zhuqiang
 * @date : 2019/5/22 22:58
 */
public class HotProductTopology {
    public static void main(String[] args) throws InvalidTopologyException, AuthorizationException, AlreadyAliveException, InterruptedException {
        // 构建拓扑，也就是手动定义业务流程
        // 其他的提交到 storm 集群后，由 storm 去调度在哪些机器上启动你所定义的 拓扑
        TopologyBuilder builder = new TopologyBuilder();
        // id、spout、并发数量
        builder.setSpout(AccessLogConsumerSpout.class.getSimpleName(),
                new AccessLogConsumerSpout(), 2);
        builder.setBolt(LogParseBolt.class.getSimpleName(),
                new LogParseBolt(), 2)
                .setNumTasks(2)
                .shuffleGrouping(AccessLogConsumerSpout.class.getSimpleName());
        builder.setBolt(ProductCountBolt.class.getSimpleName(),
                new ProductCountBolt(), 2)
                .setNumTasks(2)
                .fieldsGrouping(LogParseBolt.class.getSimpleName(), new Fields("productId"));

        Config conf = new Config();
        conf.setDebug(false);
        if (args != null && args.length > 0) {
            // 表示在命令行中运行的，需要提交的 storm 集群中去
            conf.setNumWorkers(3);
            StormSubmitter.submitTopologyWithProgressBar(args[0], conf, builder.createTopology());
        } else {
            conf.setMaxTaskParallelism(3);
            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology("HotProductTopology", conf, builder.createTopology());
            TimeUnit.SECONDS.sleep(60 * 3);
            cluster.shutdown();
        }
    }
}
