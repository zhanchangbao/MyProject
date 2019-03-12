package com.jiyong.sparkstreaming;

import com.alibaba.fastjson.JSONObject;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.spark.SparkConf;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.ConsumerStrategies;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.LocationStrategies;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class spark_kafka_mysql_recommendRecord {
    public static void main(String[] args) throws InterruptedException {
        SparkConf conf = new SparkConf()
                .setAppName("sinkToMysql")
                .setMaster("local[2]");

        JavaStreamingContext jssc = new JavaStreamingContext(conf, Durations.seconds(5));
        jssc.sparkContext().setLogLevel("WARN");

        Map<String,Object> kafkaParams = new HashMap<>();
        kafkaParams.put("bootstrap.servers","10.12.64.205:9092");
        kafkaParams.put("key.deserializer", StringDeserializer.class);
        kafkaParams.put("value.deserializer", StringDeserializer.class);
        kafkaParams.put("group.id", "use_a_separate_group_id_for_each_stream");
        kafkaParams.put("auto.offset.reset", "latest");
        kafkaParams.put("enable.auto.commit", false);

        Collection<String> topics = Arrays.asList("recommendRecord");

        JavaInputDStream<ConsumerRecord<String,String>> stream = KafkaUtils.createDirectStream(
                jssc,
                LocationStrategies.PreferConsistent(),
                ConsumerStrategies.<String,String>Subscribe(topics,kafkaParams)
        );
        JavaDStream<String> messages = stream.map(record -> record.value());
        messages.print();

        // 写入MySql
        messages.foreachRDD(rdd ->{
            if(!rdd.isEmpty()){
                rdd.foreachPartition(partionRecord -> {
                    Connection connection = ConnectionPoolShop.getConnection();
                    Statement stmt = connection.createStatement();
                    while (partionRecord.hasNext()){
                        JSONObject jsonObject = JSONObject.parseObject(partionRecord.next());
                        String si_company_ID = jsonObject.getString("si_company_ID");
                        Double si_shop_ID = jsonObject.getDouble("si_shop_ID");
                        Double memberid = jsonObject.getDouble("memberid");

                        String querysql = "SELECT * FROM tb_si_shop WHERE ID = '"+si_shop_ID+"'";
                        ResultSet resultSetbase = stmt.executeQuery(querysql);
                        boolean hasNextBase = resultSetbase.next();
                        if(hasNextBase){
                            String Longitude = resultSetbase.getString("MapNaviLongitude");
                            String Latitude = resultSetbase.getString("MapNaviLatitude");
                            String querysqlshop = "SELECT * FROM shop_info WHERE si_shop_ID = '"+si_shop_ID+"'";
                            ResultSet resultSetshop = stmt.executeQuery(querysqlshop);
                            boolean hasNext = resultSetshop.next();
                            if(!hasNext){
                                String insertsql = "INSERT INTO shop_info(si_company_ID,si_shop_ID,memberid,Longitude,Latitude,obtainted,recommend,saturation) VALUES('" + si_company_ID + "','" + si_shop_ID + "','" + memberid + "','" + Longitude + "','" + Latitude + "',0,1,0)";
                                stmt.executeUpdate(insertsql);
                            }else {
                                int recommend = resultSetshop.getInt("recommend");
                                recommend = recommend + 1;
                                int obtainted = resultSetshop.getInt("obtainted");
                                double saturation = (double) obtainted/(recommend);
                                String updatesql = "UPDATE shop_info SET Longitude = '"+Longitude+"',Latitude = '"+Latitude+"',recommend = '"+recommend+"',saturation = '"+saturation+"' WHERE si_shop_ID = '"+si_shop_ID+"'";
                                stmt.executeUpdate(updatesql);
                            }
                        }

                    }
                });
            }
        });

       jssc.start();
        jssc.awaitTermination();
    }
    }
