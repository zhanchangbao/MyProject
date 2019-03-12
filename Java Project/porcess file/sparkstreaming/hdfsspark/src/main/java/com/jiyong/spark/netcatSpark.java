package com.jiyong.spark;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import scala.Tuple2;

import java.util.Arrays;

public class netcatSpark {
    public static void main(String[] args) throws InterruptedException {
        // 创建spark配置对象
        SparkConf conf = new SparkConf().setAppName("netcatSpark");
        // 创建Streaming,设置pdd流5秒一算
        JavaStreamingContext jsc = new JavaStreamingContext(conf, Durations.seconds(5));
        // 创建接收流
        JavaReceiverInputDStream<String> lines = jsc.socketTextStream("10.12.64.229", 9999);
        // 压扁
        JavaDStream<String> words = lines.flatMap(x -> Arrays.asList(x.split(" ")).iterator());
        // 映射kv
        JavaPairDStream<String, Integer> kvRdd = words.mapToPair(
                new PairFunction<String, String, Integer>(){
                    public Tuple2<String, Integer> call(String s) throws Exception {
                        return new Tuple2<String, Integer>(s, 1);
                    }
                }
        );
        //聚合
        JavaPairDStream<String, Integer> resRDD = kvRdd.reduceByKey(
                new Function2<Integer, Integer, Integer>(){
                    public Integer call(Integer integer, Integer integer2) throws Exception {
                        return integer + integer2;
                    }
                }
        );
        // 打印
        resRDD.print();
        // 开启接收套接字上下文
        jsc.start();
        // 等待terminal的kill指令
        jsc.awaitTermination();
    }

}

