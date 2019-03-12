package com.jiyong.sparkstreaming;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.LinkedList;


public class ConnectionPoolrtb {
    //静态的connection队列
    private static LinkedList<Connection> connectionQueue;

    static {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    /*
    * 获取连接，多线程并发控制
    * */
    public synchronized static Connection getConnection() {
        try {
            if (connectionQueue == null) {
                connectionQueue = new LinkedList<Connection>();
                for (int i = 0; i < 10; i++) {
                    Connection conn = DriverManager.getConnection(
                            "jdbc:mysql://10.12.64.250:3306/rtb_preprd?useSSL=false",
                            "root",
                            "P@ss#Rtb1122");
                    connectionQueue.push(conn);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return connectionQueue.poll();
    }
    /*
    * 还回连接
    * */
    public  static void returnConnection(Connection conn){
        connectionQueue.push(conn);
    }
}