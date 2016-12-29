package com.yougou.canal;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import kafka.serializer.StringEncoder;
import java.util.Properties;
/**
 * Created by liuti on 2016/12/29.
 */
public class KafkaProducer {
    String topic;
    public KafkaProducer(String topics){
        super();
        this.topic=topics;
    }
    public static void sendMsg(String topic, String sendKey, String data){
        Producer producer = createProducer();
        producer.send(new KeyedMessage<String, String>(topic,sendKey,data));
        System.out.println("sendKey:"+sendKey);
    }

    public static Producer<Integer,String> createProducer(){
        Properties properties = new Properties();
        properties.put("zookeeper.connect", Constants.zk_Cluster);
        properties.put("serializer.class", StringEncoder.class.getName());
        properties.put("group.id", Constants.groupID);
        properties.put("metadata.broker.list",Constants.kafka_brokers);
        return new Producer<Integer,String>(new ProducerConfig(properties));
    }

    public static void main(String args[]) {
        Producer producer = createProducer();
        for (int i=1;i<=5;i++){
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            producer.send(new KeyedMessage<String, String>("yg_binLog","key"+i,"values\t"+i));
        }

    }
}
