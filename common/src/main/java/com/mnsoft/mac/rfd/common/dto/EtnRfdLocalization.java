package com.mnsoft.mac.rfd.common.dto;

/**
 * Created by goldgong on 2018-08-21.
 */
public class EtnRfdLocalization {

    /* 주행 ID */
    private String pathId;
    /* 주행 좌표 순번 */
    private long pathSeq;
    /* GPS 기록 순번 */
    private long gpsSeq;
    /* 검출 ID */
    private long detectId;
    /* 1. lane side,
        2. road link,
        3. road edge,
        4, traffic loght,
        5. traffic sign,
        6. road sign,
        7. road mark,
        8. facility */
    private LargeCode largeCode;
    /* 측위 타입 */
    private long localizationType;
    /* X */
    private long localizationX;
    /* Y */
    private long localizationY;
    /* 넓이 */
    private long localizationWidth;
    /* 높이 */
    private long localizationHeight;

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

    public long getLocalizationType() {
        return localizationType;
    }

    public void setLocalizationType(long localizationType) {
        this.localizationType = localizationType;
    }

    public long getLocalizationX() {
        return localizationX;
    }

    public void setLocalizationX(long localizationX) {
        this.localizationX = localizationX;
    }

    public long getLocalizationY() {
        return localizationY;
    }

    public void setLocalizationY(long localizationY) {
        this.localizationY = localizationY;
    }

    public long getLocalizationWidth() {
        return localizationWidth;
    }

    public void setLocalizationWidth(long localizationWidth) {
        this.localizationWidth = localizationWidth;
    }

    public long getLocalizationHeight() {
        return localizationHeight;
    }

    public void setLocalizationHeight(long localizationHeight) {
        this.localizationHeight = localizationHeight;
    }

    public LargeCode getLargeCode() {
        return largeCode;
    }

    public void setLargeCode(LargeCode largeCode) {
        this.largeCode = largeCode;
    }
}
