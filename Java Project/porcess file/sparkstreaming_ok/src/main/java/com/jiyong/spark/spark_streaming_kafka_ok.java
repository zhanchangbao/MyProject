/*
package com.jiyong.spark;

import org.apache.spark.sql.*;
import org.apache.spark.sql.streaming.StreamingQuery;
import org.apache.spark.sql.streaming.StreamingQueryException;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;

import java.sql.*;

public final class spark_streaming_kafka_ok {

    public static void main(String[] args) throws StreamingQueryException, SQLException, AnalysisException {

        SparkSession spark = SparkSession
                .builder()
                .appName("spark_streaming_kafka").master("local[2]")
                .getOrCreate();

        StructType schema = new StructType().add("shopId", DataTypes.StringType).add("memberId",DataTypes.StringType).
                add("orderId", DataTypes.StringType).add("timestamp", DataTypes.TimestampType);

        Dataset<Row> streamingInputDF = spark
                .readStream()
                .format("kafka")
                .option("kafka.bootstrap.servers", "10.12.64.205:9092")
                .option("subscribe", "greetings")
                .load();

        streamingInputDF.selectExpr("CAST(key AS STRING)", "CAST(value AS STRING)");

        Dataset<Row> selectDF = streamingInputDF.select(functions.from_json(streamingInputDF.col("value").cast("string"),
                DataType.fromJson(schema.json())).as("data")).select("data.*");

        Dataset<Row> recommendDF = selectDF.groupBy("shopId").count();

        */
/*StreamingQuery query = recommendDF.writeStream()
                .outputMode("complete")
                .format("console")
                .start();*//*



        StreamingQuery query = recommendDF.writeStream()
                .outputMode("complete").foreach(new ForeachWriter<Row>() {

                    @Override
                    public boolean open(long partitionId, long version) {

                        try {
                            Class.forName("com.mysql.jdbc.Driver");
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }

                        try {
                            Connection connection = DriverManager.getConnection("jdbc:mysql://10.12.64.250:3306/shop_info", "root", "P@ss#Rtb1122");
                            Statement statement = connection.createStatement();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        return true;
                    }

                    @Override
                    public void process(Row value) {
                        String insertsql = "INSERT INTO member_consume(shop_id,consume_count) VALUES()";
                        value.getString();
                        statement.executeUpdate(insertsql);
                    }

                    @Override
                    public void close(Throwable errorOrNull) {
                        connection.close();
                    }
                }).start();

        query.awaitTermination();

    }
}
*/
