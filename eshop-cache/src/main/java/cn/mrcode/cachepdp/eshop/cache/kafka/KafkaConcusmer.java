package cn.mrcode.cachepdp.eshop.cache.kafka;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import cn.mrcode.cachepdp.eshop.cache.service.CacheService;
import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;

/**
 * @author : zhuqiang
 * @date : 2019/4/7 16:25
 */
public class KafkaConcusmer implements Runnable {

    private final ConsumerConnector consumer;
    private final String topic;
    private CacheService cacheService;

    public KafkaConcusmer(String topic, CacheService cacheService) {
        consumer = Consumer.createJavaConsumerConnector(createConsumerConfig(
                "192.168.99.170:2181," +
                        "192.168.99.171:2181," +
                        "192.168.99.172:2181",
                "eshop-cache-group"));
        this.topic = topic;
        this.cacheService = cacheService;
    }

    private static ConsumerConfig createConsumerConfig(String a_zookeeper, String a_groupId) {
        Properties props = new Properties();
        props.put("zookeeper.connect", a_zookeeper);
        props.put("group.id", a_groupId);
        props.put("zookeeper.session.timeout.ms", "40000");
        props.put("zookeeper.sync.time.ms", "200");
        props.put("auto.commit.interval.ms", "1000");
        return new ConsumerConfig(props);
    }

    @Override
    public void run() {
        Map<String, Integer> topicCountMap = new HashMap<>();
        topicCountMap.put(topic, 1);
        Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumer.createMessageStreams(topicCountMap);
        List<KafkaStream<byte[], byte[]>> streams = consumerMap.get(topic);

        for (final KafkaStream stream : streams) {
            new Thread(new KafkaMessageProcessor(stream, cacheService)).start();
        }
    }

}