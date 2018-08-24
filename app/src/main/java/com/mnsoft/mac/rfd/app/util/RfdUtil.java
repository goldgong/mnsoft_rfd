package com.mnsoft.mac.rfd.app.util;

import com.mnsoft.mac.rfd.protobuff.RFDProto20180601_2;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by goldgong on 2018-08-21.
 */
public class RfdUtil {
    public static String getPathInfoRedisKey(int vehicleId) {
        return vehicleId + "_PATH_INFO";
    }

    public static String getPathsArryRedisKey(int vehicleId) {
        return vehicleId + "_PATHS_ARRY";
    }

    public static String getPathSeqRedisKey(int vehicleId) {
        return vehicleId + "_PATH_SEQ";
    }

    public static String getGpsSeqRedisKey(int vehicleId) {
        return vehicleId + "_GPS_SEQ";
    }

    public static String getLaneDetectedIdRedisKey(int vehicleId) {
        return vehicleId + "_LANE_SEQ";
    }

    public static String getLocalizationDetectedIdRedisKey(int vehicleId) {
        return vehicleId + "_LOCALIZATION_SEQ";
    }

    public static long getFastestGpsTime(List<RFDProto20180601_2.RFD> rfdList) {
        long fastestGpstime = 0;
        for(RFDProto20180601_2.RFD rfd : rfdList) {
            if(fastestGpstime == 0) {
                fastestGpstime = rfd.getGps().getTime();
            }
            if(fastestGpstime > rfd.getGps().getTime()) {
                fastestGpstime = rfd.getGps().getTime();
            }
        }
        return fastestGpstime;
    }

    /**
     *
     * @param latestGpstime 이전 가장 최근 값이 없으면 null
     * @param rfdList
     * @return
     */
    public static long getLatestGpsTime(Long latestGpstime, List<RFDProto20180601_2.RFD> rfdList) {
        for(RFDProto20180601_2.RFD rfd : rfdList) {
            if(latestGpstime == null) {
                latestGpstime = rfd.getGps().getTime();
            }
            if(latestGpstime < rfd.getGps().getTime()) {
                latestGpstime = rfd.getGps().getTime();
            }
        }
        return latestGpstime;
    }

    /**
     * rfd의 주행 geometry string을 반환한다.
     *   - geometry : [<key value>,<key value>. ...]
     * @param rfdList
     * @return <key value>,<key value>. ...
     */
    public static String getPartXyzm(List<RFDProto20180601_2.RFD> rfdList) {
        //Map<gpsTime, RFD> : 정렬 후 get에 필요
        Map<Long, RFDProto20180601_2.RFD> rfdMap = new HashMap();
        rfdList.forEach(rfd -> {
            rfdMap.put(rfd.getGps().getTime(), rfd);
        });
        //gpsTime 정렬
        Long[] gpsTimeArry = new Long[rfdList.size()];
        rfdMap.keySet().toArray(gpsTimeArry);
        Arrays.sort(gpsTimeArry);

        StringBuilder partXyzmBuilder = new StringBuilder();
        //geometry string 생성
        for (Long gpsTime : gpsTimeArry) {
            RFDProto20180601_2.RFD rfd = rfdMap.get(gpsTime);
            partXyzmBuilder.append(",")
                    .append(rfd.getGps().getLat()).append(" ")
                    .append(rfd.getGps().getLon()).append(" ")
                    .append(rfd.getGps().getAlt()).append(" ")
                    .append(rfd.getGps().getHeading());
        }

        return partXyzmBuilder.substring(1);
    }
}
