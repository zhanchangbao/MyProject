package com.jiyong.sparkstreaming;

import com.alibaba.fastjson.JSONObject;
import org.apache.spark.SparkConf;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class jsonMysql {
    public static void main(String[] args) throws InterruptedException {
        SparkConf conf = new SparkConf()
                .setAppName("sinkToMysql")
                .setMaster("local[2]");

        JavaStreamingContext jssc = new JavaStreamingContext(conf, Durations.seconds(5));
        jssc.sparkContext().setLogLevel("WARN");

        JavaReceiverInputDStream<String> messages = jssc.socketTextStream("10.12.64.229",9999);
        messages.print();

        // 写入MySql
        messages.foreachRDD(rdd ->{
            if(!rdd.isEmpty()){
                rdd.foreachPartition(partionRecord -> {
                    Connection connection = ConnectionPoolShop.getConnection();
                    Statement stmt = connection.createStatement();
                    while (partionRecord.hasNext()){
                        JSONObject jsonObject = JSONObject.parseObject(partionRecord.next());
                        String shopid = jsonObject.getString("shopid");
                        String memberid = jsonObject.getString("memberid");
                        Integer num = jsonObject.getInteger("num");
                        String querysql = "SELECT shopid FROM test2 WHERE shopid = '"+shopid+"'";
                        ResultSet resultSet = stmt.executeQuery(querysql);
                        boolean hasNext = resultSet.next();
                        if(!hasNext){
                            String insertsql = "INSERT INTO test2(shopid,memberid,num) VALUES('" + shopid + "'," + memberid + "," + num + ")";
                            stmt.executeUpdate(insertsql);
                        }else {
                            int oldnum = resultSet.getInt(num);
                            int newnum = oldnum + num;
                            String updatesql = "UPDATE test2 SET num = '"+newnum+"' WHERE shopid = '"+shopid+"'";
                            stmt.executeUpdate(updatesql);
                        }

                    }
                });
            }
        });
        jssc.start();
        jssc.awaitTermination();
    }
}
