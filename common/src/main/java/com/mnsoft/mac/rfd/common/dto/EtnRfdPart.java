package com.mnsoft.mac.rfd.common.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by goldgong on 2018-08-22.
 */
public class EtnRfdPart {

    private EtnRfdGps etnRfdGps = new EtnRfdGps();
    private List<EtnRfdLane> etnRfdLaneList = new ArrayList<EtnRfdLane>();
    private List<EtnRfdLocalization> etnRfdLocalizationList = new ArrayList<EtnRfdLocalization>();

    public EtnRfdGps getEtnRfdGps() {
        return etnRfdGps;
    }

    public void setEtnRfdGps(EtnRfdGps etnRfdGps) {
        this.etnRfdGps = etnRfdGps;
    }

    public List<EtnRfdLane> getEtnRfdLaneList() {
        return etnRfdLaneList;
    }

    public void setEtnRfdLaneList(List<EtnRfdLane> etnRfdLaneList) {
        this.etnRfdLaneList = etnRfdLaneList;
    }

    public List<EtnRfdLocalization> getEtnRfdLocalizationList() {
        return etnRfdLocalizationList;
    }

    public void setEtnRfdLocalizationList(List<EtnRfdLocalization> etnRfdLocalizationList) {
        this.etnRfdLocalizationList = etnRfdLocalizationList;
    }
}
