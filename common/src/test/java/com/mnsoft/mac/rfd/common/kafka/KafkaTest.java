package com.mnsoft.mac.rfd.common.kafka;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

/**
 * Created by goldgong on 2018-06-20.
 */
public class KafkaTest {

    @Test
    public void connectTest() {
//        String zookeeperUrl="192.168.203.101:2181,192.168.203.105:2181";
        String zookeeperUrl="192.168.203.105:2181";

        List<String> brokerNodes = null;
        boolean isConnect = false;
        try {
            ZooKeeper zk = new ZooKeeper(zookeeperUrl, 1000, null);
            List<String> ids = zk.getChildren("/brokers/ids", false);
            isConnect = ids != null && ids.size() > 0;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void kafkaTextSendTest() {
        KafkaSender kafkaSender = KafkaFactory.getTextSenderInstance("192.168.203.101:9092,192.168.203.105:9092", "ames_test");
//        KafkaSender kafkaHelper = new KafkaSender("server01:90920,server02:9092");
        kafkaSender.sendMessage("sample message1");
        kafkaSender.sendMessage("sample message2");
        kafkaSender.close();
    }

    class ThreadRunner {
        private Runnable runnable;
        public ThreadRunner(Runnable runnable) {
            this.runnable = runnable;
        }

        public void doWork() {
            Thread run = new Thread(this.runnable);
            run.start();
            try {
                run.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
//    @Test
    public void kafkaRecvTest() {
        TextReceiver consumer = new TextReceiver<Integer, String>();
        KafkaReceiver kafkaReceiver = KafkaFactory.getTextReceiverInstance("192.168.203.101:9092,192.168.203.105:9092", "ames_test", consumer);
        ThreadRunner runner = new ThreadRunner(kafkaReceiver);
        runner.doWork();
    }

}
