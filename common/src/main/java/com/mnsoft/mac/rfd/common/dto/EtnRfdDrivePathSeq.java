package com.mnsoft.mac.rfd.common.dto;

/**
 * Created by goldgong on 2018-08-21.
 */
public class EtnRfdDrivePathSeq {

    /* 주행 ID */
    private String pathId;
    /* 주행 좌표 순번 */
    private long pathSeq;
    /* 주행 좌표 (Geometry) */
    private String partXyzm;

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

    public String getPartXyzm() {
        return partXyzm;
    }

    public void setPartXyzm(String partXyzm) {
        this.partXyzm = partXyzm;
    }
}
