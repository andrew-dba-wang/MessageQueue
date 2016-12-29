package com.yg.kafka.sink;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import org.apache.flume.*;
import org.apache.flume.conf.Configurable;
import org.apache.flume.sink.AbstractSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Map;
import java.util.Properties;
/**
 * Created by liutizhong on 2016/4/26.
 * A Flume Sink that can publish messages to Kafka.
 * This is a general implementation that can be used with any Flume agent and a channel.
 * This supports key and messages of type String.
 * Extension points are provided to for users to implement custom key and topic extraction
 * logic based on the message content as well as the Flume context.
 * Without implementing this extension point(MessagePreprocessor), it's possible to publish
 * messages based on a static topic. In this case messages will be published to a random
 * partition.
 */
<<<<<<< HEAD:yg-consumer/src/main/java/flume/kafka/sink/KafkaSink.java
/*
*经验证，使用该sink 必须配置
* tokafka.sinks.yg_sik_9092.preprocessor=flume.kafka.impl.SimpleMessagePreprocessor
*
 */
public class KafkaSink extends AbstractSink implements Configurable {
=======
public class KafkaSink extends AbstractSink implements Configurable,Serializable {
>>>>>>> origin/master:yg-consumer/src/main/java/com/yg/kafka/sink/KafkaSink.java
    private  static final Logger logger=LoggerFactory.getLogger(KafkaSink.class);
    private  Properties properties;
    private  Producer<String,String> producer;
    private  MessagePreprocessor messagePreProcessor;
    private  String topic;
    private  Context context;


    @Override
    public Status process() throws EventDeliveryException {
        Status result=Status.READY;
        Channel channel =getChannel();
        Transaction transaction =channel.getTransaction();
        Event event=null;
        String eventTopic=topic;
        String eventKey=null;
        try{
            transaction.begin();
            event =channel.take();

            if(event != null){
                String eventBody=new String(event.getBody());
                if(messagePreProcessor !=null){
                    eventBody=messagePreProcessor.transformMessage(event,context);
                    eventTopic=messagePreProcessor.extractTopic(event,context);
                    eventKey=messagePreProcessor.extractKey(event,context);
                }
                if(logger.isDebugEnabled()){
                    logger.debug("{Event}"+eventBody);
                }
                KeyedMessage<String,String> data=new KeyedMessage<String, String>(eventTopic,eventKey,eventBody);
                producer.send(data);
            }else{
                result=Status.BACKOFF;
            }
            transaction.commit();
        }catch (Exception ex){
            transaction.rollback();
            String errorMsg="Failed to publish event:" + event;
            logger.error(errorMsg);
            //throw new EventDeliveryException(errorMsg,ex);
        }finally {
            transaction.close();
        }
        return result;
    }

    @Override
    public void configure(Context context) {
        this.context=context;
        Map<String,String> params=context.getParameters();
        properties=new Properties();
        for(String key : params.keySet()){
            if(!key.equals("type")&& !key.equals("channel")){
                properties.setProperty(key, context.getString(key));
            }
        }
        // get the message Preprocessor if set
        String preprocessorClassName =context.getString(Constants.PREPROCESSOR);
        // if it's set create an instance using Java Reflection.
        if (preprocessorClassName != null) {
            try {
                Class preprocessorClazz = Class.forName(preprocessorClassName.trim());
                Object preprocessorObj = preprocessorClazz.newInstance();
                if (preprocessorObj instanceof MessagePreprocessor) {
                    messagePreProcessor = (MessagePreprocessor) preprocessorObj;
                } else {
                    String errorMsg = "Provided class for MessagePreprocessor does not implement " +
                            "'org.yougou.flume.sink.MessagePreprocessor'";
                    logger.error(errorMsg);
                    throw new IllegalArgumentException(errorMsg);
                }
            } catch (ClassNotFoundException e) {
                String errorMsg = "Error instantiating the MessagePreprocessor implementation.";
                logger.error(errorMsg, e);
                throw new IllegalArgumentException(errorMsg, e);
            } catch (InstantiationException e) {
                String errorMsg = "Error instantiating the MessagePreprocessor implementation.";
                logger.error(errorMsg, e);
                throw new IllegalArgumentException(errorMsg, e);
            } catch (IllegalAccessException e) {
                String errorMsg = "Error instantiating the MessagePreprocessor implementation.";
                logger.error(errorMsg, e);
                throw new IllegalArgumentException(errorMsg, e);
            }
        }

        if (messagePreProcessor != null) {
            // MessagePreprocessor is not set. So read the topic from the config.
            topic = context.getString(Constants.TOPIC, Constants.DEFAULT_TOPIC);
            if (topic.equals(Constants.DEFAULT_TOPIC)) {
                logger.warn("The Properties 'metadata.extractor' or 'topic' is not set. Using the default topic name" +
                        Constants.DEFAULT_TOPIC);
            } else {
                logger.info("Using the static topic: " + topic);
            }
        }
        producer=new Producer<String, String>(new ProducerConfig(properties));
    }

    @Override
    public synchronized void start() {
        ProducerConfig config=new ProducerConfig(properties);
        producer =new Producer<String, String>(config);
        super.start();
    }

    @Override
    public synchronized void stop() {
        producer.close();
        super.stop();
    }

    @Override
    public String toString() {
        return super.toString();
    }
}