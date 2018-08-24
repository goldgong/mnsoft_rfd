package com.mnsoft.mac.rfd.app.spark.structured

import org.apache.spark.sql.types._

object JsonDFSchema {

  // level 1 주행 궤적 정보
  def sendData = new StructType()
    .add("createTime", StringType)
    .add("vehicleInfo", vehicleInfo)
    .add("cameraInfo", cameraInfo)
    .add("rfd", new ArrayType(new StructType()
      .add("gps", gps)
      .add("lane", new ArrayType(lane, true))
      .add("rm", new ArrayType(rm, true))
      .add("rs", new ArrayType(rs, true))
      .add("ts", new ArrayType(ts, true))
      .add("tl", new ArrayType(tl, true))
      , false));

  // level 2 차량 정보
  def vehicleInfo = new StructType()
      .add("vehicleId", StringType)
      .add("length", StringType)
      .add("height", StringType)
      .add("width", StringType);

  // level 2 카메라 정보
  def cameraInfo = new StructType()
    .add("type", StringType)
    .add("x", StringType)
    .add("y", StringType)
    .add("z", StringType);

  // level 3 GPS 정보
  def gps = new StructType()
      .add("time", StringType)
      .add("lat", StringType)
      .add("lon", StringType)
      .add("alt", StringType)
      .add("heading", StringType)
      .add("speed", StringType)
      .add("driveLane", StringType);

  // level 3 차선 정보
  def lane = new StructType()
    .add("type", StringType)
    .add("color", StringType)
    .add("pos", StringType);

  // level 3 - 측위 정보 ?
  def rm = new StructType()
    .add("type", StringType)
    .add("x", StringType)
    .add("y", StringType)
    .add("width", StringType)
    .add("height", StringType);

  // level 3 - 측위 정보 ?
  def rs = new StructType()
    .add("type", StringType)
    .add("x", StringType)
    .add("y", StringType)
    .add("width", StringType)
    .add("height", StringType);

  // level 3 - 측위 정보 ?
  def ts = new StructType()
    .add("type", StringType)
    .add("x", StringType)
    .add("y", StringType)
    .add("width", StringType)
    .add("height", StringType);

  // level 3 - 측위 정보 ?
  def tl = new StructType()
    .add("type", StringType)
    .add("x", StringType)
    .add("y", StringType)
    .add("width", StringType)
    .add("height", StringType);
}
