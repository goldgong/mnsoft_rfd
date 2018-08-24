package com.mnsoft.mac.rfd.common.dto;

/**
 * Created by goldgong on 2018-08-22.
 */
public enum LargeCode {
    LANE_SIDE(1)
    ,ROAD_LINK(2)
    ,ROAD_EDGE(3)
    ,TRAFFIC_LIGHT(4)
    ,TRAFFIC_SIGN(5)
    ,ROAD_SIGN(6)
    ,ROAD_MARK(7)
    ,FACILITY(8);

    private final int code;

    LargeCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
