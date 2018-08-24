package com.mnsoft.mac.rfd.app.redis;

import com.mnsoft.mac.rfd.common.util.JsonUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Redis Data Access Object
 * Created by goldgong on 2018-08-03.
 */
public class RedisDao {
    private RedisSyncClientWrapper redisClient;

    public RedisDao(RedisSyncClientWrapper redisClient) {
        this.redisClient = redisClient;
    }
    /**
     * vehicleId에 대한 pathId, pathSeq 를 설정
     *   - "_PATH" 는 redis scan pattern 검색위해 추가
     * @param pathInfoRedisKey
     * @param pathInfo
     * @return
     */
    public boolean setPathInfo(String pathInfoRedisKey, PathInfo pathInfo) {
        String rslt = redisClient.set(pathInfoRedisKey, JsonUtil.objectToJson(pathInfo));
        return rslt.equals("OK") ? true : false;
    }

    /**
     * vehicleId에 대한 현재 pathId, pathSeq 를 Map으로 반환
     * @param pathInfoKey
     * @return
     */
    public PathInfo getPathInfo(String pathInfoKey) {
        Object value = redisClient.get(pathInfoKey);
        return value == null ? null : JsonUtil.jsonToObject((String) value, PathInfo.class);
    }


    /**
     *
     * @param keys
     * @return Long integer-reply The number of keys that were removed.
     */
    public Long delPropertie(String... keys) {
        return redisClient.del(keys);
    }

    /**
     * vehicleIdPathSeq 하위의 각 GpsSeq의 Geometry(xyzm) 정보를
     *   gpsTime을 score로 하여 sorted set에 add한다
     *
     * @param pathsInfoRedisKey
     * @param gpsTime
     * @param pathPartInfo
     * @return
     */
    public boolean addGpsInfo(String pathsInfoRedisKey, long gpsTime, PathPartInfo pathPartInfo) {
        Long rtn = redisClient.zadd(pathsInfoRedisKey, gpsTime, JsonUtil.objectToJson(pathPartInfo));
        return rtn == 0 ? true : false;
    }

    /**
     * vehicleIdPathSeq의 모든 값을 gpsTime으로 오름차순 정렬하여 가져온다
     * @param pathsInfoRedisKey
     * @return
     */
    public List<PathPartInfo> getAllGpsInfo(String pathsInfoRedisKey) {
        List<PathPartInfo> allListMap = new ArrayList();

        redisClient.zrange(pathsInfoRedisKey, 0, -1).forEach(value -> {
            allListMap.add(JsonUtil.jsonToObject((String) value, PathPartInfo.class));
        });
        return allListMap;
    }

    /**
     * key의 값을 1증가하여 가져온다.
     * @param key
     * @return 기존 값이 없다면 1, 존재하면 기존값 + 1
     */
    public Long incr(String key) {
        return redisClient.incr(key);
    }

    public RedisSyncClientWrapper getRedisClient() {
        return redisClient;
    }

    public void setRedisClient(RedisSyncClientWrapper redisClient) {
        this.redisClient = redisClient;
    }
}
