package flume.kafka.source;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.message.Message;

import kafka.message.MessageAndMetadata;
import org.apache.flume.*;
import org.apache.flume.conf.Configurable;
import org.apache.flume.conf.ConfigurationException;
import org.apache.flume.event.SimpleEvent;
import org.apache.flume.source.AbstractSource;
import org.apache.flume.source.SyslogParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A Source for Kafka which reads messages from kafka. I use this in company production environment
 * and its performance is good. Over 100k messages per second can be read from kafka in one source.<p>
 * <tt>zookeeper.connect: </tt> the zookeeper ip kafka use.<p>
 * <tt>topic: </tt> the topic to read from kafka.<p>
 * <tt>group.id: </tt> the groupid of consumer group.<p>
 */
public class KafkaSource extends AbstractSource implements Configurable, PollableSource {
    private static final Logger log = LoggerFactory.getLogger(KafkaSource.class);
    private ConsumerConnector consumer;
    private ConsumerIterator<byte[], byte[]> it;
    private String topic;

    public Status process() throws EventDeliveryException {
        List<Event> eventList = new ArrayList<Event>();
        MessageAndMetadata<byte[],byte[]> message;
        Event event;
        Map<String, String> headers;
        String strMessage;
        try {
            boolean temp=it.hasNext().equals("true");
            if(temp) {
                message = it.next();
                event = new SimpleEvent();
                headers = new HashMap<String, String>();
                headers.put("timestamp", String.valueOf(System.currentTimeMillis()));

                strMessage =  String.valueOf(System.currentTimeMillis()) + "|" + new String(message.message());
                log.debug("Message: {}", strMessage);

                event.setBody(strMessage.getBytes());
                //event.setBody(message.message());
                event.setHeaders(headers);
                eventList.add(event);
            }
            getChannelProcessor().processEventBatch(eventList);
            return Status.READY;
        } catch (Exception e) {
            log.error("KafkaSource EXCEPTION, {}", e.getMessage());
            return Status.BACKOFF;
        }
    }

    public void configure(Context context) {
        topic = context.getString("topic");
        if(topic == null) {
            throw new ConfigurationException("Kafka topic must be specified.");
        }
        try {
            this.consumer = KafkaSourceUtil.getConsumer(context);
        } catch (IOException e) {
            log.error("IOException occur, {}", e.getMessage());
        } catch (InterruptedException e) {
            log.error("InterruptedException occur, {}", e.getMessage());
        }
        Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
        topicCountMap.put(topic, new Integer(1));
        Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumer.createMessageStreams(topicCountMap);
        if(consumerMap == null) {
            throw new ConfigurationException("topicCountMap is null");
        }
        List<KafkaStream<byte[], byte[]>> topicList = consumerMap.get(topic);
        if(topicList == null || topicList.isEmpty()) {
            throw new ConfigurationException("topicList is null or empty");
        }
        KafkaStream<byte[], byte[]> stream =  topicList.get(0);
        it = stream.iterator();
    }

    @Override
    public synchronized void stop() {
        consumer.shutdown();
        super.stop();
    }

}