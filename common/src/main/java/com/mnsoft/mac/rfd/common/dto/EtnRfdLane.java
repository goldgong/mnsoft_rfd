package com.mnsoft.mac.rfd.common.dto;

/**
 * Created by goldgong on 2018-08-21.
 */
public class EtnRfdLane {

    /* 주행 ID */
    private String pathId;
    /* 주행 좌표 순번 */
    private long pathSeq;
    /* GPS 기록 순번 */
    private long gpsSeq;
    /* 검출 ID */
    private long detectId;
    /* 차선 종류 */
    private long laneType;
    /* 차선 색깔 */
    private long laneColor;
    /* 차선 거리 */
    private long lanePos;

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

    public long getDetectId() {
        return detectId;
    }

    public void setDetectId(long detectId) {
        this.detectId = detectId;
    }

    public long getLaneType() {
        return laneType;
    }

    public void setLaneType(long laneType) {
        this.laneType = laneType;
    }

    public long getLaneColor() {
        return laneColor;
    }

    public void setLaneColor(long laneColor) {
        this.laneColor = laneColor;
    }

    public long getLanePos() {
        return lanePos;
    }

    public void setLanePos(long lanePos) {
        this.lanePos = lanePos;
    }
}
