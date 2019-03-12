package com.jiyong.sparkstreaming;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

public class structuredkafkatest {
    public static void main(String[] args) {
        SparkSession spark = SparkSession
                .builder()
                .appName("structuredkafka")
                .getOrCreate();

        Dataset<Row> streamingInputDF = spark
                .readStream()

    }
}
