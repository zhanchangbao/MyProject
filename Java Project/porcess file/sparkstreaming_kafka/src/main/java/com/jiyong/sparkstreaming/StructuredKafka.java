package com.jiyong.sparkstreaming;

import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.streaming.StreamingQuery;

import java.util.Arrays;

public final class StructuredKafka {

  public static void main(String[] args) throws Exception {
    if (args.length < 3) {
      System.err.println("Usage: JavaStructuredKafkaWordCount <bootstrap-servers> " +
        "<subscribe-type> <topics>");
      System.exit(1);
    }

    SparkSession spark = SparkSession
      .builder()
      .appName("StructuredKafka")
      .getOrCreate();

    // Create DataSet representing the stream of input lines from kafka
    Dataset<String> streamingInputDF = spark
      .readStream()
      .format("kafka")
      .option("kafka.bootstrap.servers", "10.12.64.205:9092")
      .option("subscribe", "ActivityRecord")
      .load()
      .selectExpr("CAST(value AS STRING)")
      .as(Encoders.STRING());

      streamingInputDF.printSchema();

    // Generate running word count
    /*Dataset<Row> wordCounts = lines.flatMap(
        (FlatMapFunction<String, String>) x -> Arrays.asList(x.split(" ")).iterator(),
        Encoders.STRING()).groupBy("value").count();

    // Start running the query that prints the running counts to the console
    StreamingQuery query = wordCounts.writeStream()
      .outputMode("complete")
      .format("console")
      .start();*/

//    query.awaitTermination();
  }
}
