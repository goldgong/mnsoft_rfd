package com.mnsoft.mac.rfd.app.kafka;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.Parser;
import com.mnsoft.mac.rfd.common.kafka.KafkaReceiverCallback;
import com.mnsoft.mac.rfd.protobuff.RFDProto20180601_2;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;

/**
 * todo: 일단 메시지 받으면 뿌려놓게 했음. 이후 단 처리 위해 위임자 추가 필요.
 * Created by goldgong on 2018-06-21.
 */
public class ProtoReceiver<T extends Message> implements KafkaReceiverCallback<ConsumerRecords<Integer, byte[]>> {

    private Parser<T> parser;
    public void setParser(Parser<T> parser) {
        this.parser = parser;
    }

    @Override
    public void consumerCallback(ConsumerRecords<Integer, byte[]> records) {
        try {
            for (ConsumerRecord<Integer, byte[]> record : records) {
                T protoObj  = parser.parseFrom(record.value());
                System.out.println("Received message: (" + record.key() + ", " + protoObj.toString() + ") at offset " + record.offset());
                System.out.println("lane.type:" + ((RFDProto20180601_2.SendData)protoObj).getRfd(0).getLane(0).getTypeValue());
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }
}
