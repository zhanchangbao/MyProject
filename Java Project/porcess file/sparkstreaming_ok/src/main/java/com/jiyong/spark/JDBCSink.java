package com.jiyong.spark;

import org.apache.spark.sql.ForeachWriter;
import org.apache.spark.sql.Row;

import java.io.Serializable;
import java.sql.*;

public class JDBCSink extends ForeachWriter<Row> implements Serializable {

    private final static String url = "jdbc:mysql://10.12.64.250:3306/shop_info?useSSL=false";
    private final static String user = "root";
    private final static String pwd = "P@ss#Rtb1122";

    private static Connection connection = null;
    private static Statement statement = null;

    @Override
    public boolean open(long partitionId, long version) {

        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        try {
            connection = DriverManager.getConnection(url,user,pwd);
            statement = connection.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return true;
    }

    @Override
    public void process(Row value) {

        String insertsql = "INSERT INTO test(shop_id,consume_count) VALUES('" + value.getAs("shopId") + "','" + value.getAs("count") + "')";
        try {
            statement.executeUpdate(insertsql);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void close(Throwable errorOrNull) {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
