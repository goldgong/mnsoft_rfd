package com.mnsoft.mac.rfd.common.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by goldgong on 2018-08-22.
 */
public class EtnRfd {
    private EtnRfdDrivePath etnRfdDrivePath = new EtnRfdDrivePath();
    private EtnRfdVehicle etnRfdVehicle = new EtnRfdVehicle();
    private EtnRfdCamera etnRfdCamera = new EtnRfdCamera();
    private EtnRfdDrivePathSeq etnRfdDrivePathSeq = new EtnRfdDrivePathSeq();
    private List<EtnRfdPart> etnRfdPartList = new ArrayList<EtnRfdPart>();

    public EtnRfdDrivePath getEtnRfdDrivePath() {
        return etnRfdDrivePath;
    }

    public void setEtnRfdDrivePath(EtnRfdDrivePath etnRfdDrivePath) {
        this.etnRfdDrivePath = etnRfdDrivePath;
    }

    public EtnRfdVehicle getEtnRfdVehicle() {
        return etnRfdVehicle;
    }

    public void setEtnRfdVehicle(EtnRfdVehicle etnRfdVehicle) {
        this.etnRfdVehicle = etnRfdVehicle;
    }

    public EtnRfdCamera getEtnRfdCamera() {
        return etnRfdCamera;
    }

    public void setEtnRfdCamera(EtnRfdCamera etnRfdCamera) {
        this.etnRfdCamera = etnRfdCamera;
    }

    public EtnRfdDrivePathSeq getEtnRfdDrivePathSeq() {
        return etnRfdDrivePathSeq;
    }

    public void setEtnRfdDrivePathSeq(EtnRfdDrivePathSeq etnRfdDrivePathSeq) {
        this.etnRfdDrivePathSeq = etnRfdDrivePathSeq;
    }

    public List<EtnRfdPart> getEtnRfdPartList() {
        return etnRfdPartList;
    }

    public void setEtnRfdPartList(List<EtnRfdPart> etnRfdPartList) {
        this.etnRfdPartList = etnRfdPartList;
    }
}
