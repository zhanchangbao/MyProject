package com.jiyong.spark;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.api.java.function.VoidFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import scala.Tuple2;

/**
 * 使用java开发本地测试的wordCount程序
 * @author meng
 *
 */
public class WordCountCluster {

	private static final Logger logger = LoggerFactory.getLogger(WordCountCluster.class);

	public static void main(String[] args) {

		//创建sparkConf对象，设置spark应用的配置信息
		SparkConf conf = new SparkConf()
				.setAppName("WordCount")
				//spark应用程序要连接的spark集群的master节点的url，local代表的是本地运行
				.setMaster("local");
		//.setMaster("spark://ip:port");

		//创建JavaSparkContext对象
		JavaSparkContext sc = new JavaSparkContext(conf);

		//针对输入源（hdfs文件、本地文件等）创建一个初始的RDD
		JavaRDD<String> lines = sc.textFile("hdfs://10.12.64.229:8020/flume/exec3/20190219/16");

		//对初始RDD进行transformation操作，如flatMap、mapToPair、reduceByKey

		//将每一行拆分成单个的单词
		//FlatMapFunction的两个泛型参数代表了输入输出的类型
		JavaRDD<String> words = lines.flatMap(new FlatMapFunction<String, String>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Iterator<String> call(String line) throws Exception {
				return Arrays.asList(line.split(" ")).iterator();
			}
		});

		//需要将每一个单词映射为（单词，1）的格式
		//JavaPairRDD的两个参数代表了Tuple元素的第一个值和第二个值
		JavaPairRDD<String,Integer> pairs = words.mapToPair(new PairFunction<String, String, Integer>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Tuple2<String, Integer> call(String word) throws Exception {
				return new Tuple2<String,Integer>(word,1);
			}
		});

		//需要以单词作为key，统计每个单词出现的次数
		JavaPairRDD<String,Integer> wordCounts = pairs.reduceByKey(new Function2<Integer, Integer, Integer>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Integer call(Integer v1, Integer v2) throws Exception {
				return v1 + v2;
			}
		});

		//foreach触发程序执行
		wordCounts.foreach(new VoidFunction<Tuple2<String,Integer>>() {

			private static final long serialVersionUID = 1L;

			@Override
			public void call(Tuple2<String, Integer> wordCount) throws Exception {
				System.out.println(wordCount._1 + " appeared " + wordCount._2 + " times.");
			}
		});

//		sc.close();

	}
}
