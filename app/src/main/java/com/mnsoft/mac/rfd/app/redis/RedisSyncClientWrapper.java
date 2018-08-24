package com.mnsoft.mac.rfd.app.redis;

import io.lettuce.core.AbstractRedisClient;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulConnection;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisKeyCommands;
import io.lettuce.core.api.sync.RedisSortedSetCommands;
import io.lettuce.core.api.sync.RedisStringCommands;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;

import java.time.Duration;
import java.util.List;

/**
 * redis standalone, cluster 모드 지원 유틸
 * Created by goldgong on 2018-08-02.
 */
public class RedisSyncClientWrapper {

    private AbstractRedisClient redisClient;
    private boolean isStandalone;
    private StatefulConnection connection;

    /**
     *
     * @param hostIp redis host ip
     * @param port redis host port
     * @param timeSecond
     * @param isStandalone true : standalone, false : cluster mode
     */
    public RedisSyncClientWrapper(String hostIp, int port, int timeSecond, boolean isStandalone) {
        this.redisClient = isStandalone ?
                RedisClient.create(new RedisURI(hostIp, port, Duration.ofSeconds(timeSecond))) :
                RedisClusterClient.create(new RedisURI(hostIp, port, Duration.ofSeconds(timeSecond)))
                ;
        this.isStandalone = isStandalone;

        this.connection = getConnection();
    }

    /**
     * Connection 생성
     * @return
     */
    private StatefulConnection getConnection() {
        return isStandalone ?
                ((RedisClient)redisClient).connect() :
                ((RedisClusterClient)redisClient).connect();
    }

    /**
     * increase sequence
     * @param key
     * @return
     */
    public Long incr(Object key) {
        return ((RedisStringCommands)getSyncCommands()).incr(key);
    }

    /**
     * set value
     * @param key
     * @param value
     * @return 'OK' or ...
     */
    public String set(Object key, Object value) {
        return ((RedisStringCommands)getSyncCommands()).set(key, value);
    }

    /**
     * return value
     * @param key
     * @return
     */
    public Object get(Object key) {
        return ((RedisStringCommands)getSyncCommands()).get(key);
    }


    /**
     *
     * @param keys
     * @return Long integer-reply The number of keys that were removed.
     */
    public Long del(Object... keys) {
        return ((RedisKeyCommands)getSyncCommands()).del(keys);
    }
    /**
     * sorted-set zadd
     * @param key
     * @param score
     * @param value
     * @return integer-reply specifically: 0, ...
     */
    public Long zadd(Object key, double score, Object value) {
        return ((RedisSortedSetCommands)getSyncCommands()).zadd(key, score, value);
    }

    /**
     * sorted-set zrange
     * @param key
     * @param startPos
     * @param endPos
     * @return
     */
    public List<Object> zrange(Object key, long startPos, long endPos) {
        return ((RedisSortedSetCommands)getSyncCommands()).zrange(key, startPos, endPos);
    }

    public void scan() {

    }

    private Object getSyncCommands() {
        if(!this.connection.isOpen()) {
            this.connection = getConnection();
        }
        return isStandalone ?
                ((StatefulRedisConnection<String, String>) this.connection).sync() :
                ((StatefulRedisClusterConnection<String, String>) this.connection).sync();
    }

    /**
     * 연결 close
     */
    public void close() {
        this.connection.close();
    }
}
