package com.mnsoft.mac.rfd.app.redis;

import com.mnsoft.mac.rfd.common.util.JsonUtil;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.cluster.api.sync.RedisAdvancedClusterCommands;
import org.junit.Test;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by goldgong on 2018-08-01.
 */
public class RedisTest {
    @Test
    public void basicStandardaloneTest() {
        //https://lettuce.io/core/release/reference/#basic.examples

//        RedisURI.create("redis://localhost/");
//        RedisURI.Builder.redis("localhost", 6379).auth("password").database(1).build();
//        RedisClient redisClient = RedisClient.create("redis://password@localhost:6379/0");
//        new RedisURI("localhost", 6379, 60, TimeUnit.SECONDS);

        RedisClient redisClient = RedisClient.create(new RedisURI("192.168.203.101", 6379, Duration.ofSeconds(60)));

//        RedisClient redisClient = RedisClient.create("redis://192.168.203.101:6379/0");
        StatefulRedisConnection<String, String> connection = redisClient.connect();
        RedisCommands<String, String> syncCommands = connection.sync();

        syncCommands.set("key", "Hello, Redis!!");
//        syncCommands.zadd("myzset2", 1, "two");

        //값 자동증가
        syncCommands.set("testincr", "1");
        System.out.println(syncCommands.incr("testincr1")); // 기존에 incr key값이 없으면 최초 증가 결과값은 1이다.
        System.out.println(syncCommands.get("testincr1"));

        syncCommands.zrange("myzset2", 0, -1).forEach(value -> {
            System.out.println(value);
        });

        //sorted set : zadd
        String vehicleIdPathSeqGpsSeq = "100_1_1";
        double gps_time = 55653000;
        Map<String, Object> item = new HashMap();
        item.put("lat", "13511802");
        item.put("lon", "45702458");
        item.put("alt", "");
        item.put("heading", "910");
        syncCommands.zadd(vehicleIdPathSeqGpsSeq, gps_time, JsonUtil.objectToJson(item));

        //sorted set : zrange all
        syncCommands.zrange(vehicleIdPathSeqGpsSeq, 0, -1).forEach(value -> {
            System.out.println(value);
        });

        connection.close();
        redisClient.shutdown();
    }

    @Test
    public void basicClusterTest() {
        // Syntax: redis://[password@]host[:port]
//        RedisClusterClient redisClient = RedisClusterClient.create("redis://192.168.203.101:7003");
        RedisClusterClient redisClient = RedisClusterClient.create("redis://172.27.0.99:7001");
//        RedisClusterClient redisClient = RedisClusterClient.create("redis://172.27.0.99:7001,172.27.0.99:7002,172.27.0.99:7003");

        StatefulRedisClusterConnection<String, String> connection = redisClient.connect();

        System.out.println("Connected to Redis");

        RedisAdvancedClusterCommands<String, String> syncCommands = connection.sync();

        syncCommands.set("key", "Hello, Redis!");


        //sorted set : zadd
        String vehicleIdPathSeqGpsSeq = "100_1_4";
        double gps_time = 55654000;
        Map<String, Object> item = new HashMap();
        item.put("lat", "13511801");
        item.put("lon", "45702465");
        item.put("alt", "");
        item.put("heading", "788");
        System.out.println(syncCommands.zadd(vehicleIdPathSeqGpsSeq, gps_time, JsonUtil.objectToJson(item)));

        //sorted set : zrange all
        syncCommands.zrange(vehicleIdPathSeqGpsSeq, 0, -1).forEach(value -> {
            System.out.println(value);
        });

        connection.close();
        redisClient.shutdown();
    }
}
