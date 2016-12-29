package flume.kafka.source;

/**
 * Created by liuti on 2016/11/18.
 */
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.javaapi.consumer.ConsumerConnector;

import org.apache.flume.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class KafkaSourceUtil {
    private static final Logger log = LoggerFactory.getLogger(KafkaSourceUtil.class);

    public static Properties getKafkaConfigProperties(Context context) {
        log.info("context={}",context.toString());
        Properties props = new Properties();
        Map<String, String> contextMap = context.getParameters();
        for(String key : contextMap.keySet()) {
            if (!key.equals("type") && !key.equals("channel")) {
                props.setProperty(key, context.getString(key));
                log.info("key={},value={}",key,context.getString(key));
            }
        }
        return props;
    }
    public static ConsumerConnector getConsumer(Context context) throws IOException, InterruptedException {
        ConsumerConfig consumerConfig = new ConsumerConfig(getKafkaConfigProperties(context));
        ConsumerConnector consumer = Consumer.createJavaConsumerConnector(consumerConfig);
        return consumer;
    }
}