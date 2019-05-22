package cn.mrcode.cachepdp.eshop.storm;

import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.LinkedTransferQueue;

import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.message.MessageAndMetadata;

/**
 * 消费 kafka 数据的 spout
 *
 * @author : zhuqiang
 * @date : 2019/5/22 23:01
 */
public class AccessLogConsumerSpout extends BaseRichSpout {
    private LinkedTransferQueue<String> queue;
    private SpoutOutputCollector collector;

    @Override
    public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
        queue = new LinkedTransferQueue();
        this.collector = collector;
        startKafka();
    }

    @Override
    public void nextTuple() {
        try {
            // 使用 LinkedTransferQueue 的目的是：
            // kafka put 会一直阻塞，直到有一个 take 执行，才会返回
            // 这里能真实的反应客户端消费 kafka 的能力
            // 而不是无限消费，存在内存中
            String message = queue.take();
            collector.emit(new Values(message));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("message"));
    }

    private ConsumerConnector consumer;
    private String topic;

    private void startKafka() {
        consumer = Consumer.createJavaConsumerConnector(createConsumerConfig(
                "192.168.99.170:2181," +
                        "192.168.99.171:2181," +
                        "192.168.99.172:2181",
                "eshop-cache-group"));
        this.topic = "access-log";
        new Thread(new Runnable() {
            @Override
            public void run() {
                Map<String, Integer> topicCountMap = new HashMap<>();
                topicCountMap.put(topic, 1);
                Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumer.createMessageStreams(topicCountMap);
                List<KafkaStream<byte[], byte[]>> streams = consumerMap.get(topic);

                for (final KafkaStream stream : streams) {
                    ConsumerIterator<byte[], byte[]> it = stream.iterator();
                    while (it.hasNext()) {
                        MessageAndMetadata<byte[], byte[]> next = it.next();
                        String message = new String(next.message());
                        try {
                            queue.transfer(message);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();
    }

    private ConsumerConfig createConsumerConfig(String a_zookeeper, String a_groupId) {
        Properties props = new Properties();
        props.put("zookeeper.connect", a_zookeeper);
        props.put("group.id", a_groupId);
        props.put("zookeeper.session.timeout.ms", "40000");
        props.put("zookeeper.sync.time.ms", "200");
        props.put("auto.commit.interval.ms", "1000");
        return new ConsumerConfig(props);
    }
}
