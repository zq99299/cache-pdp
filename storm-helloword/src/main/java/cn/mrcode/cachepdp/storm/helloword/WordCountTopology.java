package cn.mrcode.cachepdp.storm.helloword;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.apache.storm.utils.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * <pre>
 *     需求：统计一些句子中单词出现的次数
 * </pre>
 *
 * @author : zhuqiang
 * @date : 2019/5/19 13:56
 */
public class WordCountTopology {

    /**
     * 定义一个数据源；这里直接伪造一个假数据
     */
    public static class RandomSentenceSpout extends BaseRichSpout {
        private static Logger logger = Logger.getLogger(RandomSentenceSpout.class.getName());
        private Random random;
        private SpoutOutputCollector collector;
        private String[] sentences;

        /**
         * <pre>
         * 对 spout 进行初始化工作
         * 比如：创建一个线程池、创建一个数据库连接、构造一个 httpclient
         * </pre>
         *
         * @param collector 数据写出对象
         */
        @Override
        public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
            random = new Random();
            this.collector = collector;
            sentences = new String[]{"the cow jumped over the moon",
                    "an apple a day keeps the doctor away",
                    "four score and seven years ago",
                    "snow white and the seven dwarfs",
                    "i am at two with nature"};
            logger.info("RandomSentenceSpout open");
        }

        /**
         * <pre>
         *     本类（Spout）最终会运行在 task 中，某个 worker 进程的某个 executor 线程内部的某个 task 中
         *     该 task 会负责无限循环调用 nextTuple 方法
         *     就可以达到不断的发射最新的数据，形成一个数据流
         * </pre>
         */
        @Override
        public void nextTuple() {
            Utils.sleep(1000);
            String sentence = this.sentences[random.nextInt(this.sentences.length)];
            System.err.println("RandomSentenceSpout sentence:" + sentence);
            collector.emit(new Values(sentence));
        }

        /**
         * <pre>
         * 定义发射出去的每个 tuple 中的每个 field 的名称是什么？
         * 这里只有一个值，只需要写一个字段名称
         * </pre>
         */
        @Override
        public void declareOutputFields(OutputFieldsDeclarer declarer) {
            declarer.declare(new Fields("sentence"));
        }
    }

    /**
     * <pre>
     *     定义一个 bolt ，用于对数据的加工，
     *     这里拆分接收到的句子，拆分成一个一个的单词
     * </pre>
     */
    public static class SplitSentence extends BaseRichBolt {
        private OutputCollector collector;

        /**
         * 该类初始化方法，这里可以拿到发射器
         */
        @Override
        public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
            this.collector = collector;
        }

        /**
         * 每接收到一条数据，就会调用该方法，进行加工处理
         */
        @Override
        public void execute(Tuple input) {
            String sentence = input.getStringByField("sentence");
            for (String word : sentence.split(" ")) {
                // 拆分成一个一个单词之后，再发射出去
                collector.emit(new Values(word));
            }
        }

        @Override
        public void declareOutputFields(OutputFieldsDeclarer declarer) {
            // 定义发数据的字段名称
            declarer.declare(new Fields("word"));
        }
    }

    /**
     * 在定义一个 bolt ，用于对单词的统计
     */
    public static class WordCount extends BaseRichBolt {
        private OutputCollector collector;
        /**
         * 用来存储每个单词的统计数量
         */
        private Map<String, Integer> counts;

        @Override
        public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
            this.collector = collector;
            this.counts = new HashMap<>();
        }

        @Override
        public void execute(Tuple input) {
            String word = input.getStringByField("word");
            Integer count = counts.get(word);
            if (count == null) {
                count = 1;
                counts.put(word, count);
            }
            counts.put(word, ++count);
            System.err.println(Thread.currentThread().getName() + "WordCount word:" + word + ", count :" + count);
            collector.emit(new Values(word, count));
        }

        @Override
        public void declareOutputFields(OutputFieldsDeclarer declarer) {
            declarer.declare(new Fields("wordk", "count"));
        }
    }

    public static void main(String[] args) throws InvalidTopologyException, AuthorizationException, AlreadyAliveException, InterruptedException {
        // 构建拓扑，也就是手动定义业务流程
        // 其他的提交到 storm 集群后，由 storm 去调度在哪些机器上启动你所定义的 拓扑
        TopologyBuilder builder = new TopologyBuilder();
        // id、spout、并发数量
        builder.setSpout(RandomSentenceSpout.class.getSimpleName(),
                new RandomSentenceSpout(), 2);
        builder.setBolt(SplitSentence.class.getSimpleName(),
                new SplitSentence(), 5)
                // 默认是一个 executor 一个 task
                // 这里设置 5 个 executor，但是 task 设置了 10 个，相当于 每个 executor 2 个 task
                .setNumTasks(10)
                // 配置该 bolt 以何种方式从哪里获取数据
                .shuffleGrouping(RandomSentenceSpout.class.getSimpleName());
        builder.setBolt(WordCount.class.getSimpleName(),
                new WordCount(), 5)
                .setNumTasks(10)
                // 配置按字段形式去 SplitSentence 中获取数据
                // 相同的单词始终都会被发射到同一个 task 中去
                .fieldsGrouping(SplitSentence.class.getSimpleName(), new Fields("word"));

        // 上面代码配置有点像是主动获取数据，实际上是被动接受吗？

        Config conf = new Config();
        conf.setDebug(false);
        if (args != null && args.length > 0) {
            // 表示在命令行中运行的，需要提交的 storm 集群中去
            conf.setNumWorkers(3);
            StormSubmitter.submitTopologyWithProgressBar(args[0], conf, builder.createTopology());
        } else {
            conf.setMaxTaskParallelism(3);
            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology("word-count", conf, builder.createTopology());
            TimeUnit.SECONDS.sleep(20);
            cluster.shutdown();
        }
    }
}
