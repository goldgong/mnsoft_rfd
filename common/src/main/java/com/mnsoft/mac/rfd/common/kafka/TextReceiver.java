package com.mnsoft.mac.rfd.common.kafka;

import com.mnsoft.mac.rfd.common.dto.EtnRfd;
import com.mnsoft.mac.rfd.common.util.JsonUtil;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;

/**
 * Created by goldgong on 2018-06-21.
 */
public class TextReceiver<K, V> implements KafkaReceiverCallback<ConsumerRecords<K, V>> {

    @Override
    public void consumerCallback(ConsumerRecords<K, V> records) {
        for (ConsumerRecord<K, V> record : records) {
            System.out.println("Received message: (" + record.key() + ", " + record.value() + ") at offset " + record.offset());

            String jsonStr = (String) record.value();
            EtnRfd etnRfd = JsonUtil.jsonToObject(jsonStr, EtnRfd.class);
            System.out.println(etnRfd.toString());
        }
    }
}
