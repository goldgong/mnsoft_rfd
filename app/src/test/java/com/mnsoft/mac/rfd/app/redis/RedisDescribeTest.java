package com.mnsoft.mac.rfd.app.redis;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.models.partitions.RedisClusterNode;
import io.lettuce.core.cluster.pubsub.RedisClusterPubSubListener;
import io.lettuce.core.cluster.pubsub.StatefulRedisClusterPubSubConnection;
import io.lettuce.core.cluster.pubsub.api.sync.RedisClusterPubSubCommands;
import io.lettuce.core.pubsub.RedisPubSubListener;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.async.RedisPubSubAsyncCommands;
import io.lettuce.core.pubsub.api.reactive.RedisPubSubReactiveCommands;
import io.lettuce.core.pubsub.api.sync.RedisPubSubCommands;
import org.junit.Before;
import org.junit.Test;

import java.time.Duration;

/**
 * keyspace event 를 describe 테스트
 *   - cluster에서 redis는 event를 broadcast를 하지 않아
 *     localMode(client와 local로 연결된 파티션 or node) 제외한 파티션에 대한 event 수신 안됨
 *   - timeout을 시스템 시간의 timeout이 아닌, gpsTime과 현재시간 시간차를 timeout으로 보도록 하여
 *     keyspace의 describe 테스트 중단
 *
 * Created by goldgong on 2018-08-01.
 */
public class RedisDescribeTest {
    // 자체 lock 용 thread
    static class ThreadRunner {
        private Runnable runnable = () -> {
                while(true) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };

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

    private RedisClient client;

    private StatefulRedisPubSubConnection<String, String> connection;

    private String channelPattern = "__keyspace@0__:*"; // * -> redis key


    RedisPubSubListener redisPubSubListener = new RedisPubSubListener<String, String>() {
        @Override
        public void message(String channel, String message) {
            System.out.println("message:" + channel + " , " + message);
        }

        @Override
        public void message(String pattern, String channel, String message) {
            if(message.equals("expired")) {
                System.out.println("expired message:" + pattern + " , " + channel + " , " + message);
            } else {
                System.out.println("message:" + pattern + " , " + channel + " , " + message);
            }
        }

        @Override
        public void subscribed(String channel, long count) {
            System.out.println("subscribed:" + channel + "," + count);
        }

        @Override
        public void psubscribed(String pattern, long count) {
            System.out.println("psubscribed:" + pattern + "," + count);
        }

        @Override
        public void unsubscribed(String channel, long count) {
            System.out.println("unsubscribed:" + channel + "," + count);
        }

        @Override
        public void punsubscribed(String pattern, long count) {
            System.out.println("punsubscribed:" + pattern + "," + count);
        }
    };

    RedisClusterPubSubListener redisClusterPubSubListener = new RedisClusterPubSubListener<String, String>() {
        @Override
        public void message(RedisClusterNode node, String channel, String message) {
            System.out.println("message:" + node.getNodeId() + " , " + channel + " , " + message);
        }

        @Override
        public void message(RedisClusterNode node, String pattern, String channel, String message) {
            if(message.equals("expired")) {
                System.out.println("expired message:" + node.getNodeId() + " , "  + pattern + " , " + channel + " , " + message);
            } else {
                System.out.println("message:" + node.getNodeId() + " , "  + pattern + " , " + channel + " , " + message);
            }
        }

        @Override
        public void subscribed(RedisClusterNode node, String channel, long count) {
            System.out.println("subscribed:" + node.getNodeId() + " , "  + channel + "," + count);
        }

        @Override
        public void psubscribed(RedisClusterNode node, String pattern, long count) {
            System.out.println("psubscribed:" + node.getNodeId() + " , "  + pattern + "," + count);
        }

        @Override
        public void unsubscribed(RedisClusterNode node, String channel, long count) {
            System.out.println("unsubscribed:" + node.getNodeId() + " , "  + channel + "," + count);
        }

        @Override
        public void punsubscribed(RedisClusterNode node, String pattern, long count) {
            System.out.println("punsubscribed:" + node.getNodeId() + " , "  + pattern + "," + count);
        }
    };


    @Before
    public void openPubSubConnection() {
        client = RedisClient.create(new RedisURI("192.168.203.101", 6379, Duration.ofSeconds(60)));
        connection = client.connectPubSub();

    }

    /**
     * @throws Exception
     */
    @Test
    public void subscribeStandaloneTest() throws Exception {
        RedisPubSubAsyncCommands<String, String> pubsub = connection.async();
        pubsub.getStatefulConnection().addListener(redisPubSubListener); // or connection.addListener(this);

        pubsub.psubscribe(channelPattern);

        ThreadRunner lock = new ThreadRunner();
        lock.doWork();

//        pubsub.getStatefulConnection().close(); // 실행 안됨...
    }

    @Test
    public void subscribeReactStandaloneTest() {
        String channelPattern = "channel1";

        RedisPubSubReactiveCommands<String, String> pubsub = connection.reactive();
        pubsub.getStatefulConnection().addListener(redisPubSubListener); // or connection.addListener(this);
        pubsub.subscribe(channelPattern).subscribe();
        pubsub.observeChannels().doOnNext(patternMessage -> {
            String channel = patternMessage.getChannel();  //이거... publish 에만 반응. filter 위해 사용하는듯. https://github.com/lettuce-io/lettuce-core/wiki/Pub-Sub
            String message = patternMessage.getMessage();
            System.out.println(channel + ":" + message);
        }).subscribe();

        //Test 메소드 실행 후, publish를 redis 에서 실행
//        pubsub.publish(channelPattern, "msg1");
//        pubsub.publish(channelPattern, "msg2");
//        pubsub.publish(channelPattern, "msg3");
        ThreadRunner lock = new ThreadRunner();
        lock.doWork();

//        pubsub.getStatefulConnection().close(); // 실행 안됨...

    }

    /**
     * 된다! 그런데, redis-cli에서 한개 파티션(노드. 예> 99번)에 대해서만 keyspace 이벤트를 수신받는다...
     *    - (클러스터 나머지 두개 (7002, 7003) node에 대해서도 수신 받을 수 있어야 한다.)
     *    - RedisTest의 basicClusterTest() 로 키만 바꿔서 수행해도 마찬가지로 일부만 수신받았다.
     *
     * <b>추가사항 - keyspace 이벤트는 standalone 모드에서 수신이 가능하다
     * cluster 모드는 redis 서버에서 event broadcast를 하지 않는데,
     *   HA인 Sentinel 에서는 notify를 한다고 하는데, 구성 및 테스트를 해보지는 않았다.</b>
     *
     */
    @Test
    public void subscribeClusterTest() {
//        String channelPattern = "__keyspace@0__:*"; // * -> redis key
        String channelPattern = "__keyspace@0__:*"; // * -> redis key
        RedisClusterClient redisClient = RedisClusterClient.create("redis://172.27.0.99:7001");
//        RedisClusterClient redisClient = RedisClusterClient.create("redis://172.27.0.99:7001,172.27.0.99:7002,172.27.0.99:7003");

        StatefulRedisClusterPubSubConnection<String, String> connection = redisClient.connectPubSub();


        connection.addListener(redisClusterPubSubListener); // or connection.addListener(this);
//        connection.addListener(redisPubSubListener); // or connection.addListener(this);
        connection.setNodeMessagePropagation(true);

        RedisClusterPubSubCommands<String, String> pubsub = connection.sync();
        pubsub.masters().commands().psubscribe(channelPattern);


        ThreadRunner lock = new ThreadRunner();
        lock.doWork();
    }

    /**
     * 이것도 keyspace를 세개 node(7001, 7002, 7003) 중 한개 노드만 수신된다.. (Sentinel 미구성)
     */
    @Test
    public void subscribeClusterNodeTest() {
        String channelPattern = "__keyspace@0__:*"; // * -> redis key
        RedisClusterClient redisClient = RedisClusterClient.create("redis://172.27.0.99:7001");

        StatefulRedisClusterPubSubConnection<String, String> connection = redisClient.connectPubSub();
        connection.setNodeMessagePropagation(true);

//        RedisClusterNode partition = connection.getPartitions().getPartition(0);
        connection.getPartitions().forEach(partition -> {
            StatefulRedisPubSubConnection<String, String> node = connection.getConnection(partition.getNodeId());

            node.addListener(redisPubSubListener); // or connection.addListener(this);

            RedisPubSubCommands<String, String> nodePubsub = node.sync();
            nodePubsub.psubscribe(channelPattern);
        });

        ThreadRunner lock = new ThreadRunner();
        lock.doWork();


    }

    /**
     * 이것도 keyspace를 세개 node(7001, 7002, 7003) 중 한개 노드만 수신된다.. (Sentinel 미구성)
     *   - 아래 코드는 테스트를 위함. 의미없음.
     *
     */
    @Test
    public void subscribeCluster3NodeTest() {
//        String channelPattern = "__keyspace@0__:*"; // * -> redis key
        String channelPattern = "__keyspace@0__:*"; // * -> redis key
        RedisClusterClient redisClient1 = RedisClusterClient.create("redis://172.27.0.99:7001");
        RedisClusterClient redisClient2 = RedisClusterClient.create("redis://172.27.0.99:7002");
        RedisClusterClient redisClient3 = RedisClusterClient.create("redis://172.27.0.99:7003");

        StatefulRedisClusterPubSubConnection<String, String> connection1 = redisClient1.connectPubSub();
        StatefulRedisClusterPubSubConnection<String, String> connection2 = redisClient2.connectPubSub();
        StatefulRedisClusterPubSubConnection<String, String> connection3 = redisClient3.connectPubSub();


        connection1.addListener(redisClusterPubSubListener); // or connection.addListener(this);
        connection2.addListener(redisClusterPubSubListener); // or connection.addListener(this);
        connection3.addListener(redisClusterPubSubListener); // or connection.addListener(this);
//        connection.addListener(redisPubSubListener); // or connection.addListener(this);
        connection1.setNodeMessagePropagation(true);
        connection2.setNodeMessagePropagation(true);
        connection3.setNodeMessagePropagation(true);

        RedisClusterPubSubCommands<String, String> pubsub1 = connection1.sync();
        RedisClusterPubSubCommands<String, String> pubsub2 = connection2.sync();
        RedisClusterPubSubCommands<String, String> pubsub3 = connection3.sync();

        pubsub1.masters().commands().psubscribe(channelPattern);
        pubsub2.masters().commands().psubscribe(channelPattern);
        pubsub3.masters().commands().psubscribe(channelPattern);


        ThreadRunner lock = new ThreadRunner();
        lock.doWork();


    }

    //////////////////////위에까지 테스트 완료 ///////////////////////////////

    /**
     * cluster 에서 keyspace event 못받아온다....망할 왜안대지??
     */
    @Test
    public void subscribeReactClusterTest() {
        String channelPattern = "channel1";
        RedisClusterClient redisClient = RedisClusterClient.create("redis://192.168.203.101:7003");

        StatefulRedisClusterPubSubConnection<String, String> connection = redisClient.connectPubSub();

        RedisPubSubReactiveCommands<String, String> pubsub = connection.reactive();
        connection.addListener(redisClusterPubSubListener); // or connection.addListener(this);
        connection.setNodeMessagePropagation(true);

        pubsub.psubscribe(channelPattern).subscribe();
        pubsub.observeChannels().doOnNext(patternMessage -> {
            String channel = patternMessage.getChannel();  //이거... publish 에만 반응. filter 위해 사용하는듯. https://github.com/lettuce-io/lettuce-core/wiki/Pub-Sub
            String message = patternMessage.getMessage();
            System.out.println(channel + ":" + message);

        }).subscribe();

        ThreadRunner lock = new ThreadRunner();
        lock.doWork();

        pubsub.getStatefulConnection().close(); // 실행 안됨...

    }
}
