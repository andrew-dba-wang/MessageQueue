package flume.kafka;

import java.util.Map;
import java.util.Properties;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

import org.apache.flume.Channel;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.Transaction;
import org.apache.flume.conf.Configurable;
import org.apache.flume.conf.ConfigurationException;
import org.apache.flume.sink.AbstractSink;

/**
 * Created by liuti on 2016/9/26.
 */
public class KafkaSink extends AbstractSink implements Configurable {
    private String topic;
    private Producer<String,byte[]> producer;

    public void configure(Context context){
        topic = context.getString("topic");
        if(topic==null){
            throw new ConfigurationException("Kafka topic must be specified.");
        }
        Properties props=new Properties();
        //props ??????
        Map<String,String> contextMap=context.getParameters();
        for(String key : contextMap.keySet()){
            if(!key.equals("type")&& !key.equals("channel")){
                props.setProperty(key, context.getString(key));
            }
        }
        producer=new Producer<String, byte[]>(new ProducerConfig(props));
    }
    @Override
    public void start(){

    }
    @Override
    public void stop(){

    }
    public Status process() throws EventDeliveryException{
        Channel channel=getChannel();
        Transaction tx=channel.getTransaction();
        try{
            tx.begin();
            Event e=channel.take();
            if(e==null)
            {
                tx.rollback();
                return Status.BACKOFF;
            }
            KeyedMessage<String,byte[]> data=
                    new KeyedMessage<String, byte[]>(topic,e.getBody());
            producer.send(data);
            tx.close();
            return Status.READY;
        }catch (Exception e){
            tx.rollback();
            return Status.BACKOFF;
        }finally {
            tx.close();
        }
    }
}