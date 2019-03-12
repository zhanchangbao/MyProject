package com.jiyong.recommend;

import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spark.ConsumerStrategy;
import org.apache.rocketmq.spark.RocketMQConfig;
import org.apache.rocketmq.spark.RocketMqUtils;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.sql.catalyst.expressions.aggregate.Collect;
import org.apache.spark.sql.rocketmq.RocketMQUtils;
import org.apache.spark.storage.StorageLevel;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.*;
import scala.Tuple2;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

public class rocketmq_spark {
    public static void main(String[] args) throws InterruptedException {

        // 创建spark配置对象
        SparkConf conf = new SparkConf().setAppName("rocketmq_spark");
        // 创建Streaming,设置Rdd流5秒一算
        JavaStreamingContext jsc = new JavaStreamingContext(conf, Durations.seconds(5));

        Collection<String> topics = new ArrayList<>();
        topics.add("fsd");
        Properties properties = new Properties();
        properties.setProperty(RocketMQConfig.NAME_SERVER_ADDR, "10.12.64.206:9876");
        properties.setProperty(RocketMQConfig.CONSUMER_GROUP, "fsd");
        properties.setProperty(RocketMQConfig.CONSUMER_TOPIC, "fsd");
        // 创建接收流
        JavaInputDStream<MessageExt> stream = RocketMqUtils.createJavaMQPullStream(jsc, "fsd",
                topics, ConsumerStrategy.earliest(), true, false, false);
        // JavaInputDStream ds = RocketMQUtils.createInputDStream(jssc, properties, StorageLevel.MEMORY_ONLY());
        // 打印
        stream.print();
        // 开启接收套接收上下文
        jsc.start();
        // 等待terminal的kill指令
        jsc.awaitTerminationOrTimeout(60000);
    }
}

