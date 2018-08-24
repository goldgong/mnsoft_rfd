package com.mnsoft.mac.rfd.common.dto;

/**
 * Created by goldgong on 2018-08-21.
 */
public class EtnRfdDrivePath {

    /* 주행 ID */
    private String pathId;
    /* 주행 이름 (PATH_ID와 동일값 선적용. 추후 변경 예정) */
    private String pathName;
    /* 주행 좌표 (Geometry) */
    private String pathXyzm;
    /* 주행 종료 여부 */
    private String pathEndFlag;
    /* 주행 시작 시간 */
    private long pathStartDateTim;
    /* 주행 종료 시간 */
    private long pathEndDateTim;

    public String getPathId() {
        return pathId;
    }

    public void setPathId(String pathId) {
        this.pathId = pathId;
    }

    public String getPathName() {
        return pathName;
    }

    public void setPathName(String pathName) {
        this.pathName = pathName;
    }

    public String getPathXyzm() {
        return pathXyzm;
    }

    public void setPathXyzm(String pathXyzm) {
        this.pathXyzm = pathXyzm;
    }

    public String getPathEndFlag() {
        return pathEndFlag;
    }

    public void setPathEndFlag(String pathEndFlag) {
        this.pathEndFlag = pathEndFlag;
    }

    public long getPathStartDateTim() {
        return pathStartDateTim;
    }

    public void setPathStartDateTim(long pathStartDateTim) {
        this.pathStartDateTim = pathStartDateTim;
    }

    public long getPathEndDateTim() {
        return pathEndDateTim;
    }

    public void setPathEndDateTim(long pathEndDateTim) {
        this.pathEndDateTim = pathEndDateTim;
    }
}
