package com.mnsoft.mac.rfd.common.dto;

/**
 * Created by goldgong on 2018-08-21.
 */
public class EtnRfdCamera {

    /* 주행 ID */
    private String pathId;
    /* 카메라종류 */
    private long cameraType;
    /* 카메라 설치 X좌표 정보(CM) */
    private long cameraX;
    /* 카메라 설치 Y좌표 정보(CM) */
    private long cameraY;
    /* 카메라 설치 Z좌표 정보(CM) */
    private long cameraZ;

    public String getPathId() {
        return pathId;
    }

    public void setPathId(String pathId) {
        this.pathId = pathId;
    }

    public long getCameraType() {
        return cameraType;
    }

    public void setCameraType(long cameraType) {
        this.cameraType = cameraType;
    }

    public long getCameraX() {
        return cameraX;
    }

    public void setCameraX(long cameraX) {
        this.cameraX = cameraX;
    }

    public long getCameraY() {
        return cameraY;
    }

    public void setCameraY(long cameraY) {
        this.cameraY = cameraY;
    }

    public long getCameraZ() {
        return cameraZ;
    }

    public void setCameraZ(long cameraZ) {
        this.cameraZ = cameraZ;
    }
}
