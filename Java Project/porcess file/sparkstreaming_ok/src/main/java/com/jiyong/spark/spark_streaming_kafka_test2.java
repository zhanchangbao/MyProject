package com.jiyong.spark;

import org.apache.spark.sql.*;
import org.apache.spark.sql.streaming.StreamingQuery;
import org.apache.spark.sql.streaming.StreamingQueryException;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;

import java.sql.SQLException;

public final class spark_streaming_kafka_test2 {

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


        StreamingQuery query = recommendDF.writeStream()
                .outputMode("update")
                .foreach(new JDBCSink1())
                .start();

        query.awaitTermination();
    }
}
