package com.mnsoft.mac.rfd.app.redis;

/**
 * Redis
 * Created by goldgong on 2018-08-21.
 */
public class PathInfo {

    /* 주행경로내 구간순번 */
    private long pathSeq;
    /* vehicle_id + "_" + 첫번째 gpsTime */
    private String pathId;
    /* 주행 시작 시간 - 주행 첫번째 가장 빠른 gpsTime */
    private long fastestGpsTime;
    /* Timeout 확인 위한 가장 최근 gpsTime set */
    private long latestGpsTime;

    public long getPathSeq() {
        return pathSeq;
    }

    public void setPathSeq(long pathSeq) {
        this.pathSeq = pathSeq;
    }

    public String getPathId() {
        return pathId;
    }

    public void setPathId(String pathId) {
        this.pathId = pathId;
    }

    public long getLatestGpsTime() {
        return latestGpsTime;
    }

    public void setLatestGpsTime(long latestGpsTime) {
        this.latestGpsTime = latestGpsTime;
    }

    public long getFastestGpsTime() {
        return fastestGpsTime;
    }

    public void setFastestGpsTime(long fastestGpsTime) {
        this.fastestGpsTime = fastestGpsTime;
    }

    @Override
    public String toString() {
        return "PathInfo{" +
                "pathSeq=" + pathSeq +
                ", pathId='" + pathId + '\'' +
                ", fastestGpsTime=" + fastestGpsTime +
                ", latestGpsTime=" + latestGpsTime +
                '}';
    }
}
