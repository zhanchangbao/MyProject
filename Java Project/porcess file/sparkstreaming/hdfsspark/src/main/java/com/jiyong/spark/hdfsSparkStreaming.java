package com.jiyong.spark;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import scala.Tuple2;

import java.util.Arrays;

public class hdfsSparkStreaming {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(hdfsSparkStreaming.class);
    public static void main(String[] args) throws InterruptedException {
        // 创建spark配置对象
        SparkConf conf = new SparkConf().setAppName("JavaSparkStreamingHDFS").setMaster("local[2]");
        // 创建Streaming,设置pdd流5秒一算
        JavaSparkContext sc = new JavaSparkContext(conf);
        JavaStreamingContext jsc = new JavaStreamingContext(sc, Durations.seconds(5));
        // 创建接收流,监听hdfs指定目录中的数据
        String directory = "hdfs://10.12.64.229:8020/flume/exec3/20190220/09";
        JavaDStream<String> lines = jsc.textFileStream(directory);
        // 压扁
        JavaDStream<String> words = lines.flatMap(x -> Arrays.asList(x.split(" ")).iterator());
        // 映射kv
        JavaPairDStream<String, Integer> kvRdd = words.mapToPair((PairFunction<String, String, Integer>) s -> new Tuple2<String, Integer>(s, 1)
        );
        //聚合
        JavaPairDStream<String, Integer> resRDD = kvRdd.reduceByKey(
                (Function2<Integer, Integer, Integer>) (integer, integer2) -> integer + integer2
        );
          // 打印
        resRDD.print();
        // 开启接收套接字上下文
        jsc.start();
        // 等待terminal的kill指令
        jsc.awaitTermination();
    }

}

