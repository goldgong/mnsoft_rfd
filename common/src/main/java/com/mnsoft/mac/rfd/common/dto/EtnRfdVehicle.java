package com.mnsoft.mac.rfd.common.dto;

/**
 * Created by goldgong on 2018-08-21.
 */
public class EtnRfdVehicle {

    /* 주행 ID */
    private String pathId;
    /* 차량 ID */
    private long vehicleId;
    /* 차량길이(CM) */
    private long vehicleLength;
    /* 차량높이(CM) */
    private long vehicleHeight;
    /* 차량넓이(CM) */
    private long vehicleWidth;

    public String getPathId() {
        return pathId;
    }

    public void setPathId(String pathId) {
        this.pathId = pathId;
    }

    public long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public long getVehicleLength() {
        return vehicleLength;
    }

    public void setVehicleLength(long vehicleLength) {
        this.vehicleLength = vehicleLength;
    }

    public long getVehicleHeight() {
        return vehicleHeight;
    }

    public void setVehicleHeight(long vehicleHeight) {
        this.vehicleHeight = vehicleHeight;
    }

    public long getVehicleWidth() {
        return vehicleWidth;
    }

    public void setVehicleWidth(long vehicleWidth) {
        this.vehicleWidth = vehicleWidth;
    }
}
