package com.mnsoft.mac.rfd.common.kafka;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by goldgong on 2016-08-19.
 */
public class KafkaReceiver<K,V> implements Runnable {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private final AtomicBoolean closed = new AtomicBoolean(false);
    private KafkaConsumer<K, V> consumer ;
    private KafkaReceiverCallback callback;
    private String topic;

    public KafkaReceiver(Properties props, String topic, KafkaReceiverCallback callback) {
        consumer = new KafkaConsumer(props);
        this.topic = topic;
        this.callback = callback;
    }

    private void close() {
        try {
            if(consumer != null) {
                consumer.close();
            }
        }catch (Exception e)    {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
    }

    @Override
    public void run() {
        consumer.subscribe(Collections.singletonList(this.topic));
        while(!closed.get()) {
            ConsumerRecords<K, V> records = consumer.poll(10000);
            callback.consumerCallback(records);
        }
        close();
    }
    // Shutdown hook which can be called from a separate thread
    public void shutdown() {
        closed.set(true);
        consumer.wakeup();
    }
}
