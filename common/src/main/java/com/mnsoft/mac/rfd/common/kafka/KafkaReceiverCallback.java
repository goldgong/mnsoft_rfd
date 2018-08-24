package com.mnsoft.mac.rfd.common.kafka;

/**
 * Created by goldgong on 2018-06-21.
 */
public interface KafkaReceiverCallback<T> {

    void consumerCallback(T t);
}
