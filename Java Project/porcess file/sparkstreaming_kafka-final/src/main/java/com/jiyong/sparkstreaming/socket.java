package com.jiyong.sparkstreaming;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import scala.Tuple2;

import java.util.Arrays;


public class socket {
    public static void main(String[] args) throws InterruptedException {
        SparkConf conf = new SparkConf().setAppName("Demo2").setMaster("local[2]");
        JavaStreamingContext jssc = new JavaStreamingContext(conf, Durations.seconds(4));
        jssc.sparkContext().setLogLevel("WARN");
        JavaReceiverInputDStream<String> lines = jssc.socketTextStream("10.12.64.229",9999);
        JavaDStream<String> words = lines.flatMap(x -> Arrays.asList(x.split(" ")).iterator());
        JavaPairDStream<String,Integer> pairs = words.mapToPair(s -> new Tuple2(s,1));
        JavaPairDStream<String,Integer> wordCounts = pairs.reduceByKey((i1,i2) -> i1 + i2);
        wordCounts.print();
        jssc.start();
        jssc.awaitTermination();
    }
}
