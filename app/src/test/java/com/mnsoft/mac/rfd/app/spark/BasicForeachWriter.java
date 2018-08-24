package com.mnsoft.mac.rfd.app.spark;

import com.mnsoft.mac.rfd.protobuff.RFDProto20180601_2;
import org.apache.spark.sql.ForeachWriter;
import org.apache.spark.sql.Row;
import scala.collection.JavaConversions;

import java.util.List;

/**
 * Created by goldgong on 2018-08-21.
 */
public class BasicForeachWriter extends ForeachWriter<Row> {
    @Override
    public boolean open(long partitionId, long version) {
        return true;
    }

    @Override
    public void process(Row row) {
        RFDProto20180601_2.VehicleInfo.Builder vehicleInfoBuilder = RFDProto20180601_2.VehicleInfo.newBuilder();
        vehicleInfoBuilder.setVehicleId(row.getAs("vehicleInfo.vehicleId"));
        vehicleInfoBuilder.setLength(row.getAs("vehicleInfo.length"));
        String vehicleInfoHeight = row.getAs("vehicleInfo.height");
        String vehicleInfoWidth = row.getAs("vehicleInfo.width");
        String cameraInfoType = row.getAs("cameraInfo.type");
        String cameraInfoX = row.getAs("cameraInfo.x");
        String cameraInfoY = row.getAs("cameraInfo.y");
        String cameraInfoZ = row.getAs("cameraInfo.z");
        String rfdGpsTime = row.getAs("rfd.gps.time");
        String rfdGpsLat = row.getAs("rfd.gps.lat");
        String rfdGpsLon = row.getAs("rfd.gps.lon");
        String rfdGpsAlt = row.getAs("rfd.gps.alt");
        String rfdGpsHeading = row.getAs("rfd.gps.heading");
        String rfdGpsSpeed = row.getAs("rfd.gps.speed");
        String rfdGpsDriveLane = row.getAs("rfd.gps.driveLane");
        List<Integer> rfdLaneTypeList = JavaConversions.seqAsJavaList(row.getAs("rfd.lane.type"));
        List<Object> rfdLaneColor = JavaConversions.seqAsJavaList(row.getAs("rfd.lane.color"));
        List<Object> rfdLanePos = JavaConversions.seqAsJavaList(row.getAs("rfd.lane.pos"));
        List<Object> rfdRmType = JavaConversions.seqAsJavaList(row.getAs("rfd.rm.type"));
        List<Object> rfdRmX = JavaConversions.seqAsJavaList(row.getAs("rfd.rm.x"));
        List<Object> rfdRmY = JavaConversions.seqAsJavaList(row.getAs("rfd.rm.y"));
        List<Object> rfdRmWidth = JavaConversions.seqAsJavaList(row.getAs("rfd.rm.width"));
        List<Object> rfdRmHeight = JavaConversions.seqAsJavaList(row.getAs("rfd.rm.height"));
        List<Object> rfdRsType = JavaConversions.seqAsJavaList(row.getAs("rfd.rs.type"));
        List<Object> rfdRsX = JavaConversions.seqAsJavaList(row.getAs("rfd.rs.x"));
        List<Object> rfdRsY = JavaConversions.seqAsJavaList(row.getAs("rfd.rs.y"));
        List<Object> rfdRsWidth = JavaConversions.seqAsJavaList(row.getAs("rfd.rs.width"));
        List<Object> rfdRsHeight = JavaConversions.seqAsJavaList(row.getAs("rfd.rs.height"));
        List<Object> rfdTsType = JavaConversions.seqAsJavaList(row.getAs("rfd.ts.type"));
        List<Object> rfdTsX = JavaConversions.seqAsJavaList(row.getAs("rfd.ts.x"));
        List<Object> rfdTsY = JavaConversions.seqAsJavaList(row.getAs("rfd.ts.y"));
        List<Object> rfdTsWidth = JavaConversions.seqAsJavaList(row.getAs("rfd.ts.width"));
        List<Object> rfdTsHeight = JavaConversions.seqAsJavaList(row.getAs("rfd.ts.height"));
        List<Object> rfdTlType = JavaConversions.seqAsJavaList(row.getAs("rfd.tl.type"));
        List<Object> rfdTlX = JavaConversions.seqAsJavaList(row.getAs("rfd.tl.x"));
        List<Object> rfdTlY = JavaConversions.seqAsJavaList(row.getAs("rfd.tl.y"));
        List<Object> rfdTlWidth = JavaConversions.seqAsJavaList(row.getAs("rfd.tl.width"));
        List<Object> rfdTlHeight = JavaConversions.seqAsJavaList(row.getAs("rfd.tl.height"));

        System.out.println(row);

    }

    @Override
    public void close(Throwable errorOrNull) {

    }
}
