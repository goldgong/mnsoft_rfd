package com.mnsoft.mac.rfd.common.dto;

/**
 * Created by goldgong on 2018-08-21.
 */
public class EtnRfdGps {

    /* 주행 ID */
    private String pathId;
    /* 주행 좌표 순번 */
    private long pathSeq;
    /* GPS 기록 순번 */
    private long gpsSeq;
    /* GPS 기록 시간 */
    private long gpsTime;
    /* 위도(degree*360,000) */
    private double lat;
    /* 경도(degree*360,000) */
    private double lon;
    /* 고도(CM) */
    private long alt;
    /* 헤딩(degree Scale*10) */
    private long heading;
    /* 속도*10 */
    private long speed;
    /* 주행차선 */
    private long driveLane;

    public String getPathId() {
        return pathId;
    }

    public void setPathId(String pathId) {
        this.pathId = pathId;
    }

    public long getPathSeq() {
        return pathSeq;
    }

    public void setPathSeq(long pathSeq) {
        this.pathSeq = pathSeq;
    }

    public long getGpsSeq() {
        return gpsSeq;
    }

    public void setGpsSeq(long gpsSeq) {
        this.gpsSeq = gpsSeq;
    }

    public long getGpsTime() {
        return gpsTime;
    }

    public void setGpsTime(long gpsTime) {
        this.gpsTime = gpsTime;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public long getAlt() {
        return alt;
    }

    public void setAlt(long alt) {
        this.alt = alt;
    }

    public long getHeading() {
        return heading;
    }

    public void setHeading(long heading) {
        this.heading = heading;
    }

    public long getSpeed() {
        return speed;
    }

    public void setSpeed(long speed) {
        this.speed = speed;
    }

    public long getDriveLane() {
        return driveLane;
    }

    public void setDriveLane(long driveLane) {
        this.driveLane = driveLane;
    }
}
