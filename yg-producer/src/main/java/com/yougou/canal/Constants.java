package com.yougou.canal;

/**
 * Created by liuti on 2016/12/29.
 */
public class Constants {
    public static final String zk_Cluster="10.10.10.121:2181,10.10.10.122:2181,10.10.11.69:2181";
    //public static final String kafka_brokers="10.10.10.121:9092,10.10.10.121:9093,10.10.10.121:9094,10.10.10.122:9095,10.10.10.122:9096";
    public static final String kafka_brokers="Hadoopslave04:9092,Hadoopslave04:9093,Hadoopslave04:9094,Hadoopslave04:9091";
    public static final String topics="yg_binLog";
    public static final String groupID="binlog";

}
