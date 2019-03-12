package com.jiyong.spark;

import org.apache.spark.sql.*;
import org.apache.spark.sql.catalyst.expressions.JsonToStructs;
import org.apache.spark.sql.catalyst.expressions.JsonToStructs$;
import org.apache.spark.sql.streaming.StreamingQuery;
import org.apache.spark.sql.streaming.StreamingQueryException;
import org.apache.spark.sql.types.StructType;

import java.sql.SQLException;

import static org.codehaus.commons.compiler.samples.DemoBase.explode;

public final class spark_streaming_kafka_test {


    public static void main(String[] args) throws StreamingQueryException, SQLException, AnalysisException {

        SparkSession spark = SparkSession
                .builder()
                .appName("spark_streaming_kafka").master("local[2]")
                .getOrCreate();

        StructType schema = new StructType().add("shopId", "string").add("memberId", "string").
                add("orderId", "string").add("timestamp", "string").add("recommend", "integer");

/*        String nestTimestampFormat = "yyyy-MM-dd'T'HH:mm:ss.sss'Z'";
        Object jsonOptions = Map("timestampFormat" -> nestTimestampFormat);*/

        Dataset<Row> df = spark
                .readStream()
                .format("kafka")
                .option("kafka.bootstrap.servers", "10.12.64.205:9092")
                .option("subscribe", "greetings")
                .load();

        df.selectExpr("CAST(key AS STRING)", "CAST(value AS STRING)");
        df.printSchema();

        Dataset<Row> df1 = df.select(functions.from_json(functions.column("value").cast("string"),schema).alias("parsed_value"));
        df1.printSchema();

        Dataset<Row> df2 = df1
                .withColumn("shopId", functions.column("parsed_value"))
                .withColumn("memberId", functions.column("parsed_value"))
                .withColumn("orderId", functions.column("parsed_value"))
                .withColumn("timestamp", functions.column("parsed_value"))
                .withColumn("recommend", functions.column("parsed_value"))
                .drop(functions.column("parsed_value"));
        Dataset<Row> df3 = df2.select("shopId","memberId","orderId");
        Dataset<Row> df4 = df3.groupBy("shopId").count();

        df2.printSchema();
//        df2.show();

//        Dataset<Row> df2 = df1.select("shopId");
        StreamingQuery query = df2.writeStream()
                .outputMode("update")
                .format("console")
                .start();

        query.awaitTermination();

    }
}
