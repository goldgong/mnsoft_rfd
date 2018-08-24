package com.mnsoft.mac.rfd.app.redis;

import com.mnsoft.mac.rfd.app.util.RfdUtil;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * Created by goldgong on 2018-08-03.
 */
public class RedisDaoTest {
    private RedisDao redisDao;

    @Before
    public void init() {
        RedisSyncClientWrapper redisClient = new RedisSyncClientWrapper("172.27.0.99", 7001, 600, false);
        redisDao = new RedisDao(redisClient);
    }

    @Test
    public void setVehicleIdInfoTest() {
        PathInfo pathInfo = new PathInfo();
        pathInfo.setPathSeq(1);
        pathInfo.setPathId("pathId");
        pathInfo.setLatestGpsTime(55653000);

        System.out.println(redisDao.setPathInfo(RfdUtil.getPathInfoRedisKey(100), pathInfo));
    }

    @Test
    public void getVehicleIdInfoTest() {
        System.out.println(redisDao.getPathInfo(RfdUtil.getPathInfoRedisKey(100)));
    }

    @Test
    public void addVehicleIdPathSeqGpsSeqInfoTest() {
        int vehicleId = 100;
        long pathSeq = 1;
        addGpsInfo(redisDao, vehicleId, pathSeq);

        vehicleId = 100;
        pathSeq = 2;
        addGpsInfo(redisDao, vehicleId, pathSeq);
//        sorted set : zrange all
//        redisDao.zrange(vehicleIdPathSeqGpsSeq, 0, -1).forEach(value -> {
//            System.out.println(value);
//        });
    }

    private void addGpsInfo(RedisDao redisDao, int vehicleId, long pathSeq) {
        long gps_time = 55653000;
        PathPartInfo pathPartInfo = new PathPartInfo();
        pathPartInfo.setLat(13511802);
        pathPartInfo.setAlt(45702458);
        pathPartInfo.setHeading(910);
        System.out.println(redisDao.addGpsInfo(RfdUtil.getPathsArryRedisKey(vehicleId), gps_time, pathPartInfo));

        gps_time = 55654000;

        System.out.println(redisDao.addGpsInfo(RfdUtil.getPathsArryRedisKey(vehicleId), gps_time, pathPartInfo));

        gps_time = 55652000;
        pathPartInfo = new PathPartInfo();
        pathPartInfo.setLat(13511803);
        pathPartInfo.setAlt(45702459);
        pathPartInfo.setHeading(950);
        redisDao.addGpsInfo(RfdUtil.getPathsArryRedisKey(vehicleId), gps_time, pathPartInfo);
    }

    @Test
    public void zrangeVehicleIdPathSeqGpsSeqInfoTest() {
        int vehicleId = 100;
        long pathSeq = 1;

        List<PathPartInfo> list = redisDao.getAllGpsInfo(RfdUtil.getPathsArryRedisKey(vehicleId));
        System.out.println(list);
    }

    @Test
    public void seqTest() {
        System.out.println(redisDao.incr(RfdUtil.getGpsSeqRedisKey(100)));
        System.out.println(redisDao.incr(RfdUtil.getLaneDetectedIdRedisKey(100)));
        System.out.println(redisDao.incr(RfdUtil.getLocalizationDetectedIdRedisKey(100)));
    }
}
