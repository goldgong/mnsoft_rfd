package com.mnsoft.mac.rfd.common.kafka;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.Future;

/**
 * Created by goldgong on 2016-08-19.
 */
public class KafkaSender<K,V> {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private KafkaProducer<K, V> producer ;
    private String topic;
    private boolean sync = true;

    public KafkaSender(Properties props, String topic) {
        try {
            producer = new KafkaProducer(props);
            this.topic = topic;

        }catch (Exception e)    {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
    }

    public boolean sendMessage(V v) {
        boolean isOK = false;
        try {
            long startTime = System.currentTimeMillis();
            K key = null;
            ProducerRecord data = new ProducerRecord(topic, key ,v);
            if(sync) {
                Future<RecordMetadata> future = producer.send(data);
                RecordMetadata rmd = future.get();
                if(rmd != null ) {
                    logger.info(" +++ rmd : "+ rmd.toString());
                }
            } else {
                producer.send(data, new DemoCallBack(startTime, key, v));
            }
            isOK = true;
        }catch (Exception e1 ) {
            logger.error(ExceptionUtils.getStackTrace(e1));
        }
        return isOK;
    }

    public void close() {
        try {
            if(producer != null)    producer.close();
        }catch (Exception e)    {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
    }

    /**
     * todo: 비동기 전송이 필요한지는 아직 모르겠음..
     */
    class DemoCallBack implements Callback {

        private final long startTime;
        private final K key;
        private final V message;

        public DemoCallBack(long startTime, K key, V message) {
            this.startTime = startTime;
            this.key = key;
            this.message = message;
        }

        /**
         * A callback method the user can implement to provide asynchronous handling of request completion. This method will
         * be called when the record sent to the server has been acknowledged. Exactly one of the arguments will be
         * non-null.
         *
         * @param metadata  The metadata for the record that was sent (i.e. the partition and offset). Null if an error
         *                  occurred.
         * @param exception The exception thrown during processing of this record. Null if no error occurred.
         */
        public void onCompletion(RecordMetadata metadata, Exception exception) {
            long elapsedTime = System.currentTimeMillis() - startTime;
            if (metadata != null) {
                System.out.println(
                        "message(" + key + ", " + message + ") sent to partition(" + metadata.partition() +
                                "), " +
                                "offset(" + metadata.offset() + ") in " + elapsedTime + " ms");
            } else {
                exception.printStackTrace();
            }
        }
    }
}
