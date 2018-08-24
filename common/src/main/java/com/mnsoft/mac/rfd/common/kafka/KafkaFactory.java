package com.mnsoft.mac.rfd.common.kafka;

import java.util.Properties;

/**
 * Created by goldgong on 2018-06-21.
 */
public class KafkaFactory {

    public static KafkaSender getTextSenderInstance(String kafkaBrokerList, String topic) {
        Properties props = new Properties();
        props.put("bootstrap.servers", kafkaBrokerList);
        props.put("client.id", "rfd-producer");
        props.put("key.serializer", "org.apache.kafka.common.serialization.IntegerSerializer");
//            props.put("key.serializer", "kafka.serializer.DefaultEncoder");
//            props.put("key.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
//            props.put("value.serializer", "kafka.serializer.DefaultEncoder");
//            props.put("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");
//            props.put("partitioner.class", DefaultPartitioner.class.getName());
//            props.put("request.required.acks", "0");X
//            props.put("compression.type", "none"); //zip, snappy, lz4
//            props.put("producer.type", "sync");
        return new KafkaSender<Integer, String>(props, topic);
    }

    public static KafkaSender getProtobufSenderInstance(String kafkaBrokerList, String topic) {
        Properties props = new Properties();
        props.put("bootstrap.servers", kafkaBrokerList);
        props.put("client.id", "rfd-producer");
        props.put("key.serializer", "org.apache.kafka.common.serialization.IntegerSerializer");
//            props.put("key.serializer", "kafka.serializer.DefaultEncoder");
//            props.put("key.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");
//            props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
//            props.put("value.serializer", "kafka.serializer.DefaultEncoder");
        props.put("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");
//            props.put("partitioner.class", DefaultPartitioner.class.getName());
//            props.put("request.required.acks", "0");
//            props.put("compression.type", "none"); //zip, snappy, lz4
//            props.put("producer.type", "sync");
        return new KafkaSender<Integer, byte[]>(props, topic);
    }

    public static KafkaReceiver getTextReceiverInstance(String kafkaBrokerList, String topic, KafkaReceiverCallback callback) {
        Properties props = new Properties();
        props.put("group.id", "rfd");
        props.put("bootstrap.servers", kafkaBrokerList);
        props.put("key.deserializer", "org.apache.kafka.common.serialization.IntegerDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        return new KafkaReceiver<Integer, String>(props, topic, callback);
    }

    public static KafkaReceiver getProtobufReceiverInstance(String kafkaBrokerList, String topic, KafkaReceiverCallback callback) {
        Properties props = new Properties();
        props.put("group.id", "rfd");
        props.put("bootstrap.servers", kafkaBrokerList);
        props.put("key.deserializer", "org.apache.kafka.common.serialization.IntegerDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");

        return new KafkaReceiver<Integer, byte[]>(props, topic, callback);
    }
}
