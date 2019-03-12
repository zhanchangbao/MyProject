package com.jiyong.sparkstreaming;

import org.apache.spark.SparkConf;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import scala.Tuple2;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;

public class sinkToMysql {
    public static void main(String[] args) throws InterruptedException {

        SparkConf conf = new SparkConf()
                .setAppName("sinkToMysql")
                .setMaster("local[2]");
        JavaStreamingContext jssc = new JavaStreamingContext(conf, Durations.seconds(5));
        jssc.sparkContext().setLogLevel("WARN");

     //   jssc.checkpoint("hdfs://10.12.64.229:8020/recommend_checkpoint");

        JavaReceiverInputDStream<String> lines = jssc.socketTextStream("10.12.64.229",9999);
        JavaDStream<String> words = lines.flatMap(x -> Arrays.asList(x.split(" ")).iterator());
        JavaPairDStream<String,Integer> wordCount = words.mapToPair(x -> new Tuple2(x,1));
        JavaPairDStream<String,Integer> Count = wordCount.reduceByKey((x,y) -> (x+y));
        Count.print();

        //写入mysql
        Count.foreachRDD(rdd ->{
            if(!rdd.isEmpty()){
                rdd.foreachPartition(partitionOfRecords -> {
                    Connection connection = ConnectionPoolShop.getConnection();
                    while (partitionOfRecords.hasNext()){
                        Statement stmt = connection.createStatement();
                        String querysql = "SELECT word FROM test WHERE word  = '"+partitionOfRecords.next()._1()+"'";
                        System.out.println(querysql);
                        //根据查询结果判断，如果有该单词，就更新单词的count，如果没有就插入一条记录
                        ResultSet resultSet = stmt.executeQuery(querysql);
                        boolean hasnext = resultSet.next();
                        if (!hasnext){
                            String insertsql = "insert into test(word,count) values('" + partitionOfRecords.next()._1() + "'," + partitionOfRecords.next()._2() + ")";
                            stmt.executeUpdate(insertsql);
                        }else{
                            String updatesql = "UPDATE test SET count = "+partitionOfRecords.next()._2()+" where word = '"+partitionOfRecords.next()._1()+"'";
                            stmt.executeUpdate(updatesql);
                        }
                    }
                    ConnectionPoolShop.returnConnection(connection);
                });
            }

        });

        jssc.start();
        jssc.awaitTermination();
    }
}
