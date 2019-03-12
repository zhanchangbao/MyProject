package com.jiyong.sparkstreaming;


import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.Optional;
import org.apache.spark.api.java.function.Function3;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.State;
import org.apache.spark.streaming.StateSpec;
import org.apache.spark.streaming.api.java.*;
import scala.Tuple2;

import java.util.Arrays;
import java.util.List;

public class kafka {
    public static void main(String[] args) throws InterruptedException {
        SparkConf conf = new SparkConf().setAppName("kafka").setMaster("local[2]");
        JavaStreamingContext ssc = new JavaStreamingContext(conf, Durations.seconds(5));
        ssc.sparkContext().setLogLevel("WARN");

        ssc.checkpoint("hdfs://10.12.64.229:8020/recommend_checkpoint");
        List<Tuple2<String,Integer>> tuples = Arrays.asList(new Tuple2("hello",1),new Tuple2("world",1));
        JavaPairRDD<String,Integer> initialRDD = ssc.sparkContext().parallelizePairs(tuples);
        JavaReceiverInputDStream<String> lines = ssc.socketTextStream("10.12.64.229",9999);
        JavaDStream<String> words = lines.flatMap(x -> Arrays.asList(x.split(" ")).iterator());
        JavaPairDStream<String,Integer> wordDstream = words.mapToPair(s -> new Tuple2(s,1));

        Function3<String,Optional<Integer>,State<Integer>,Tuple2<String,Integer>> mappingFunc =
                (word,one,state) -> {
                    int sum = one.orElse(0) + (state.exists()?state.get():0);
                    Tuple2<String,Integer> output = new Tuple2<>(word,sum);
                    state.update(sum);
                    return output;
                };
        JavaMapWithStateDStream<String,Integer,Integer,Tuple2<String,Integer>> stateDStream =
                wordDstream.mapWithState(StateSpec.function(mappingFunc).initialState(initialRDD));

        stateDStream.print();
        ssc.start();
        ssc.awaitTermination();
    }

}
