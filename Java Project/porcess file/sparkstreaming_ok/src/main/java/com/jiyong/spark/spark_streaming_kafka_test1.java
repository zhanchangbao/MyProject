package com.jiyong.spark;

import org.apache.spark.sql.*;
import org.apache.spark.sql.streaming.StreamingQuery;
import org.apache.spark.sql.streaming.StreamingQueryException;
import org.apache.spark.sql.types.StructType;

import java.sql.*;

public final class spark_streaming_kafka_test1 {

    public static void main(String[] args) throws StreamingQueryException, SQLException, AnalysisException {

        SparkSession spark = SparkSession
                .builder()
                .appName("spark_streaming_kafka").master("local[2]")
                .getOrCreate();

        StructType schema = new StructType().add("shopId", "string").add("memberId", "string").
                add("orderId", "string").add("timestamp", "string");

/*        String nestTimestampFormat = "yyyy-MM-dd'T'HH:mm:ss.sss'Z'";
        Object jsonOptions = Map("timestampFormat" -> nestTimestampFormat);*/

        Dataset<Row> df = spark
                .readStream()
                .format("kafka")
                .option("kafka.bootstrap.servers", "10.12.64.205:9092")
                .option("subscribe", "greetings")
                .load()
                .select(functions.from_json(functions.col("value").cast("string"),schema).alias("parsed_value"));

        df.selectExpr("CAST(key AS STRING)", "CAST(value AS STRING)");


        StreamingQuery query = df.writeStream()
                .outputMode("update")
                .format("console")
                .start();


        /*StreamingQuery query = shopinfo.writeStream()
                .outputMode("update").foreach(new ForeachWriter<Row>() {

                    @Override
                    public boolean open(long partitionId, long version) {

                        try {
                            Class.forName("com.mysql.jdbc.Driver");
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }

                        try {
                            Connection connection = DriverManager.getConnection("jdbc:mysql://10.12.64.229:3306/test", "root", "123456");
                            Statement statement = connection.createStatement();
                            String insertsql = "INSERT INTO shop_info(si_company_ID,si_shop_ID,memberid,Longitude,Latitude,obtainted,recommend,saturation) VALUES('" + si_company_ID + "','" + si_shop_ID + "','" + memberid + "','" + Longitude + "','" + Latitude + "',0,1,0)";
                            "VALUES (" + value._1 + "," + value._2 + ")")
                            statement.executeUpdate(insertsql);
                            connection.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        return true;
                    }

                    @Override
                    public void process(Row value) {


                    }

                    @Override
                    public void close(Throwable errorOrNull) {

                    }


                }).start();*/

        query.awaitTermination();

    }
}
