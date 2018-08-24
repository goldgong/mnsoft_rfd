package com.mnsoft.mac.rfd.app.redis;

/**
 * Redis
 * Created by goldgong on 2018-08-21.
 */
public class PathPartInfo {

    private double lat;
    private double lon;
    private long alt;
    private long heading;

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

    @Override
    public String toString() {
        return "PathPartInfo{" +
                "lat=" + lat +
                ", lon=" + lon +
                ", alt=" + alt +
                ", heading=" + heading +
                '}';
    }
}
