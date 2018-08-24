package com.mnsoft.mac.rfd.app.spark;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.Parser;
import com.google.protobuf.util.JsonFormat;
import com.mnsoft.mac.rfd.app.redis.PathInfo;
import com.mnsoft.mac.rfd.app.redis.PathPartInfo;
import com.mnsoft.mac.rfd.app.redis.RedisDao;
import com.mnsoft.mac.rfd.app.redis.RedisSyncClientWrapper;
import com.mnsoft.mac.rfd.app.spark.structured.JsonDFSchema;
import com.mnsoft.mac.rfd.app.util.RfdUtil;
import com.mnsoft.mac.rfd.common.dto.*;
import com.mnsoft.mac.rfd.common.kafka.KafkaFactory;
import com.mnsoft.mac.rfd.common.kafka.KafkaSender;
import com.mnsoft.mac.rfd.common.util.JsonUtil;
import com.mnsoft.mac.rfd.protobuff.RFDProto20180601_2;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.*;
import org.apache.spark.sql.*;
import org.apache.spark.sql.expressions.UserDefinedFunction;
import org.apache.spark.sql.streaming.StreamingQueryException;
import org.apache.spark.sql.streaming.Trigger;
import org.apache.spark.sql.types.*;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.ConsumerStrategies;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.LocationStrategies;
import org.junit.Test;
import scala.Tuple2;

import java.util.*;

import static org.apache.spark.sql.functions.explode;
import static org.apache.spark.sql.functions.col;
import static org.apache.spark.sql.functions.from_json;

/**
 * Created by goldgong on 2018-06-25.
 */
public class SparkKafkaTest {

    //clear
    @Test
    public void basicTest() {
        String appName = "JavaStructuredNetworkWordCount";
        SparkSession spark = SparkSession
                .builder()
                .master("local[2]")
                .appName(appName)
                .getOrCreate();

        Dataset<Row> textContent = spark.read().text("D:/dev/20180619_엠앤소프트_개발건/엠앤소프트개발.txt");

        // Split the lines into words
        Dataset<String> words = textContent
                .as(Encoders.STRING())
                .flatMap((FlatMapFunction<String, String>) x -> Arrays.asList(x.split(" ")).iterator(), Encoders.STRING()).cache();

        // Generate running word count
        words.groupBy("value").count().show();
    }

    /**
     * byte[] -> protobuf object -> java object -> json -> kafka
     */
    @Test
    public void dstreamWithProtobufTest() {
        SparkConf conf = new SparkConf();
        conf.setAppName("rfd");
        conf.setMaster("local[2]");
//        conf.set("spark.hadoop.cloneConf", "true");

        String checkPointPath = "c:/tmp/spark/checkpoint/rfd";
        String kafkaTopic = "external-rfd";

        long batchduration = 1000;

        //5초 간격으로 spark DStream 수행
        JavaStreamingContext javaStreamingContext = new JavaStreamingContext(conf, new Duration(10000));

        Map<String, Object> kafkaParams = new HashMap<>();
        kafkaParams.put("bootstrap.servers", "localhost:9092");
        kafkaParams.put("key.deserializer", IntegerDeserializer.class);
        kafkaParams.put("value.deserializer", ByteArrayDeserializer.class);
        kafkaParams.put("group.id", "rfd");
        kafkaParams.put("auto.offset.reset", "latest");
        kafkaParams.put("enable.auto.commit", false);

        Collection<String> topics = Arrays.asList(kafkaTopic);
        Parser<RFDProto20180601_2.SendData> parser = RFDProto20180601_2.SendData.parser();

        // javaInputDStream 정의. kafka의 ConsumerRecord<[Key], [Value]> 정의
        //   - Key, Value : kafka ConsumerRecord의 Key, Value를 받는다.
        //   - 아래 코드의 ConsumerStrategies에는 파티션 관련 메소드(SubscribePattern())가 존재한다. : 테스트 필요
        JavaInputDStream<ConsumerRecord<Integer, byte[]>> javaInputDStream =
                KafkaUtils.createDirectStream(
                        javaStreamingContext,
                        LocationStrategies.PreferConsistent(),
                        ConsumerStrategies.<Integer, byte[]>Subscribe(topics, kafkaParams)
                );

        //1. javaInputDStream.foreachRdd()
        //  => java.io.NotSerializableException: org.apache.kafka.clients.consumer.ConsumerRecord 발생.
        //      : 2. javaPairDStream.foreachRdd() 사용해야함.
        //JavaRDD<ConsumerRecord<Integer, byte[]>> consumerRecordJavaRDD
        /*javaInputDStream.foreachRDD((consumerRecordJavaRDD) -> {

            //ConsumerRecord<Integer, byte[]>
            consumerRecordJavaRDD.collect().forEach((consumerRecord) -> {
                Integer key = consumerRecord.key();
                Message protoObj = null;
                try {
                    protoObj = parser.parseFrom(consumerRecord.value());
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
                System.out.println(protoObj.toString());
            });
        });*/


        //2. javaPairDStream.foreachRdd()
        //  ConsumerRecord<Integer, byte[]> -> Tuple2<Integer, byte[]> 로 변환 하여 직렬화 문제 (1.주석코드) 를 회피한다.
        JavaPairDStream<Integer, byte[]> javaPairDStream = javaInputDStream.mapToPair(record -> {
            Tuple2<Integer, byte[]> tuple2 = null;
            Integer key = record.key();
            tuple2 = new Tuple2(key, record.value());
            return tuple2;
        });

        //byte[] -> ProtobufObject
//        RedisSyncClientWrapper redisClient = new RedisSyncClientWrapper("192.168.203.101", 6379, 60, false);
        RedisSyncClientWrapper redisClient = new RedisSyncClientWrapper("172.27.0.99", 7001, 600, false);
        RedisDao redisDao = new RedisDao(redisClient);
        KafkaSender kafkaSender = KafkaFactory.getTextSenderInstance("localhost:9092", "internal-rfd");
        javaPairDStream.foreachRDD((pairRDD) -> {
                    // protobuf Message 클래스가 직렬화가 안되어, byte[]를 받아 agg 후, 최종 객체로 파싱한다.
                    pairRDD.collect().forEach((tuple2) -> {
                        Integer key = tuple2._1();
                        byte[] values = tuple2._2();

                        //1. byte[]를 hdfs에 저장한다.
                        final RFDProto20180601_2.SendData protoObj = bytesToProtobufObject(parser, values);

                        // 경로 정보 업데이트
                        PathInfo pathInfo = updatePathInfo(redisDao, protoObj);

                        // 주행 궤적정보 추가
                        addGpsInfo(redisDao, protoObj);

                        //kafka에 보낼 객체 생성
                        EtnRfd etnRfd = buildEtnRfd(redisDao, pathInfo, protoObj);

                        // EtnRfd kafka로 전송
                        String jsonStr = JsonUtil.objectToJson(etnRfd);
                        //todo: 실패시 재시도
                        boolean isOk = kafkaSender.sendMessage(jsonStr);

                        // redis에 pathInfo 업데이트
                        String pathInfoRedisKey = RfdUtil.getPathInfoRedisKey(protoObj.getVehicleInfo().getVehicleId());
                        //todo: 실패시 재시도
                        isOk = redisDao.setPathInfo(pathInfoRedisKey, pathInfo);
                    });
            }
        );

        javaStreamingContext.start();
        try {
            javaStreamingContext.awaitTermination();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            kafkaSender.close();
            redisClient.close();
        }
    }

    private EtnRfd buildEtnRfd(RedisDao redisDao, PathInfo pathInfo, RFDProto20180601_2.SendData protoObj) {
        EtnRfd etnRfd = new EtnRfd();
        //  ETM_RFD_DRIVE_PATH - 주행. 현재상태 주행중.
        EtnRfdDrivePath etnRfdDrivePath = etnRfd.getEtnRfdDrivePath();
        etnRfdDrivePath.setPathId(pathInfo.getPathId());
        etnRfdDrivePath.setPathName(pathInfo.getPathId());
        etnRfdDrivePath.setPathXyzm(null);
        etnRfdDrivePath.setPathStartDateTim(pathInfo.getFastestGpsTime());
        etnRfdDrivePath.setPathEndFlag("N");

        //  ETN_RFD_VEHICLE - 차량
        EtnRfdVehicle etnRfdVehicle = etnRfd.getEtnRfdVehicle();
        etnRfdVehicle.setPathId(pathInfo.getPathId());
        etnRfdVehicle.setVehicleId(protoObj.getVehicleInfo().getVehicleId());
        etnRfdVehicle.setVehicleLength(protoObj.getVehicleInfo().getLength());
        etnRfdVehicle.setVehicleHeight(protoObj.getVehicleInfo().getHeight());
        etnRfdVehicle.setVehicleWidth(protoObj.getVehicleInfo().getWidth());

        //  ETN_RFD_CAMERA - CameraInfo
        EtnRfdCamera etnRfdCamera = etnRfd.getEtnRfdCamera();
        etnRfdCamera.setPathId(pathInfo.getPathId());
        etnRfdCamera.setCameraType(protoObj.getCameraInfo().getType().getNumber());
        etnRfdCamera.setCameraX(protoObj.getCameraInfo().getX());
        etnRfdCamera.setCameraY(protoObj.getCameraInfo().getY());
        etnRfdCamera.setCameraZ(protoObj.getCameraInfo().getZ());

        //  ETN_RFD_DRIVE_PATH_SEQ - GPSInfo들의 주행 좌표
        String partXyzm = RfdUtil.getPartXyzm(protoObj.getRfdList());
        EtnRfdDrivePathSeq etnRfdDrivePathSeq = etnRfd.getEtnRfdDrivePathSeq();
        etnRfdDrivePathSeq.setPathId(pathInfo.getPathId());
        etnRfdDrivePathSeq.setPathSeq(pathInfo.getPathSeq());
        etnRfdDrivePathSeq.setPartXyzm(partXyzm);

        List<EtnRfdPart> etnRfdPartList = etnRfd.getEtnRfdPartList();
        //  ETN_RFD_GPS - GPSInfo
        protoObj.getRfdList().forEach(rfd -> {
            //각 seq의 시작값 설정
            // ** 주행 시작시, gpsSeq, laneDetectedId, localiDetectedId 값은 모두 1이다
            //    --> 하나의 주행 전체에서 unique해야한다.
            //    --> 주행 종료시 항상 redis에서 위 값들을 삭제한다. : redis는 없는 값을 증가시키면 1을 리턴한다.
            long gpsSeq = redisDao.incr(RfdUtil.getGpsSeqRedisKey(protoObj.getVehicleInfo().getVehicleId()));

            EtnRfdPart etnRfdPart = new EtnRfdPart();
            etnRfdPartList.add(etnRfdPart);

            EtnRfdGps etnRfdGps = etnRfdPart.getEtnRfdGps();
            etnRfdGps.setPathId(pathInfo.getPathId());
            etnRfdGps.setPathSeq(pathInfo.getPathSeq());
            etnRfdGps.setGpsSeq(gpsSeq);
            etnRfdGps.setGpsTime(rfd.getGps().getTime());
            etnRfdGps.setLat(rfd.getGps().getLat());
            etnRfdGps.setLon(rfd.getGps().getLon());
            etnRfdGps.setAlt(rfd.getGps().getAlt());
            etnRfdGps.setHeading(rfd.getGps().getHeading());
            etnRfdGps.setSpeed(rfd.getGps().getSpeed());
            etnRfdGps.setDriveLane(rfd.getGps().getDriveLane());

            //  ETN_RFD_LANE - Lane
            List<EtnRfdLane> etnRfdLaneList = etnRfdPart.getEtnRfdLaneList();
            rfd.getLaneList().forEach(lane -> {
                long laneDetectedId = redisDao.incr(RfdUtil.getLaneDetectedIdRedisKey(protoObj.getVehicleInfo().getVehicleId()));
                EtnRfdLane etnRfdLane = new EtnRfdLane();
                etnRfdLane.setPathId(pathInfo.getPathId());
                etnRfdLane.setPathSeq(pathInfo.getPathSeq());
                etnRfdLane.setGpsSeq(gpsSeq);
                etnRfdLane.setDetectId(laneDetectedId);
                etnRfdLane.setLaneType(lane.getTypeValue());
                etnRfdLane.setLaneColor(lane.getColorValue());
                etnRfdLane.setLanePos(lane.getPos());

                etnRfdLaneList.add(etnRfdLane);

            });

            //  ETN_RFD_LOCALIZATION - RoadMark, RoadSign, TrafficSign, TrafficLight
            List<EtnRfdLocalization> etnRfdLocalizationList = etnRfdPart.getEtnRfdLocalizationList();
            rfd.getRmList().forEach(rm -> {
                long localiDetectedId = redisDao.incr(RfdUtil.getLocalizationDetectedIdRedisKey(protoObj.getVehicleInfo().getVehicleId()));
                EtnRfdLocalization etnRfdLocalization = new EtnRfdLocalization();
                etnRfdLocalization.setPathId(pathInfo.getPathId());
                etnRfdLocalization.setPathSeq(pathInfo.getPathSeq());
                etnRfdLocalization.setGpsSeq(gpsSeq);
                etnRfdLocalization.setDetectId(localiDetectedId);
                etnRfdLocalization.setLargeCode(LargeCode.ROAD_MARK);
                etnRfdLocalization.setLocalizationType(rm.getTypeValue());
                etnRfdLocalization.setLocalizationX(rm.getX());
                etnRfdLocalization.setLocalizationY(rm.getY());
                etnRfdLocalization.setLocalizationWidth(rm.getWidth());
                etnRfdLocalization.setLocalizationHeight(rm.getHeight());

                etnRfdLocalizationList.add(etnRfdLocalization);
            });

            rfd.getRsList().forEach(rs -> {
                long localiDetectedId = redisDao.incr(RfdUtil.getLocalizationDetectedIdRedisKey(protoObj.getVehicleInfo().getVehicleId()));

                EtnRfdLocalization etnRfdLocalization = new EtnRfdLocalization();
                etnRfdLocalization.setPathId(pathInfo.getPathId());
                etnRfdLocalization.setPathSeq(pathInfo.getPathSeq());
                etnRfdLocalization.setGpsSeq(gpsSeq);
                etnRfdLocalization.setDetectId(localiDetectedId);
                etnRfdLocalization.setLargeCode(LargeCode.ROAD_SIGN);
                etnRfdLocalization.setLocalizationType(rs.getTypeValue());
                etnRfdLocalization.setLocalizationX(rs.getX());
                etnRfdLocalization.setLocalizationY(rs.getY());
                etnRfdLocalization.setLocalizationWidth(rs.getWidth());
                etnRfdLocalization.setLocalizationHeight(rs.getHeight());

                etnRfdLocalizationList.add(etnRfdLocalization);
            });

            rfd.getTsList().forEach(ts -> {
                long localiDetectedId = redisDao.incr(RfdUtil.getLocalizationDetectedIdRedisKey(protoObj.getVehicleInfo().getVehicleId()));

                EtnRfdLocalization etnRfdLocalization = new EtnRfdLocalization();
                etnRfdLocalization.setPathId(pathInfo.getPathId());
                etnRfdLocalization.setPathSeq(pathInfo.getPathSeq());
                etnRfdLocalization.setGpsSeq(gpsSeq);
                etnRfdLocalization.setDetectId(localiDetectedId);
                etnRfdLocalization.setLargeCode(LargeCode.TRAFFIC_SIGN);
                etnRfdLocalization.setLocalizationType(ts.getTypeValue());
                etnRfdLocalization.setLocalizationX(ts.getX());
                etnRfdLocalization.setLocalizationY(ts.getY());
                etnRfdLocalization.setLocalizationWidth(ts.getWidth());
                etnRfdLocalization.setLocalizationHeight(ts.getHeight());

                etnRfdLocalizationList.add(etnRfdLocalization);
            });

            rfd.getTlList().forEach(tl -> {
                long localiDetectedId = redisDao.incr(RfdUtil.getLocalizationDetectedIdRedisKey(protoObj.getVehicleInfo().getVehicleId()));

                EtnRfdLocalization etnRfdLocalization = new EtnRfdLocalization();
                etnRfdLocalization.setPathId(pathInfo.getPathId());
                etnRfdLocalization.setPathSeq(pathInfo.getPathSeq());
                etnRfdLocalization.setGpsSeq(gpsSeq);
                etnRfdLocalization.setDetectId(localiDetectedId);
                etnRfdLocalization.setLargeCode(LargeCode.TRAFFIC_LIGHT);
                etnRfdLocalization.setLocalizationType(tl.getTypeValue());
                etnRfdLocalization.setLocalizationX(tl.getX());
                etnRfdLocalization.setLocalizationY(tl.getY());
                etnRfdLocalization.setLocalizationWidth(tl.getWidth());
                etnRfdLocalization.setLocalizationHeight(tl.getHeight());

                etnRfdLocalizationList.add(etnRfdLocalization);
            });
        });

        return etnRfd;
    }

    private RFDProto20180601_2.SendData bytesToProtobufObject(Parser<RFDProto20180601_2.SendData> parser, byte[] values) {
        RFDProto20180601_2.SendData protoObj  = null;
        try {
            protoObj = parser.parseFrom(values);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        return protoObj;
    }

    private void addGpsInfo(RedisDao redisDao, RFDProto20180601_2.SendData protoObj) {
        // 주행 궤적정보 추가
        //  - 일단 protobuf 객체를 직접 넣어보자
        String pathsArryRedisKey = RfdUtil.getPathsArryRedisKey(protoObj.getVehicleInfo().getVehicleId());

        protoObj.getRfdList().forEach(rfd -> {
            PathPartInfo pathPartInfo = new PathPartInfo();
            pathPartInfo.setLat(rfd.getGps().getLat());
            pathPartInfo.setLon(rfd.getGps().getLon());
            pathPartInfo.setAlt(rfd.getGps().getAlt());
            pathPartInfo.setHeading(rfd.getGps().getHeading());
            boolean result = redisDao.addGpsInfo(pathsArryRedisKey, rfd.getGps().getTime(), pathPartInfo);
        });
    }

    /**
     * Redis에서 경로정보를 조회하고, 있으면 정보 업데이트, 없으면 경로정보 신규 생성.
     * @param redisDao
     * @param protoObj
     * @return 갱신된 PathInfo
     */
    private PathInfo updatePathInfo(RedisDao redisDao, RFDProto20180601_2.SendData protoObj) {
        // Message 객체에서 각 값들을 꺼내어, 각 파트별 인스턴스를 생성한다.
        String pathInfoRedisKey = RfdUtil.getPathInfoRedisKey(protoObj.getVehicleInfo().getVehicleId());

        // Redis로부터 경로정보를 가져온다
        PathInfo pathInfo = redisDao.getPathInfo(pathInfoRedisKey);

        int vehicleId = protoObj.getVehicleInfo().getVehicleId();
        String pathSeqKeyName = RfdUtil.getPathSeqRedisKey(vehicleId);

        if(pathInfo == null) {
            // Redis에 기존 경로정보가 없다면 Redis 정보(Sequence 등) 초기화 및 경로정보 생성
            long fastestGpsTime = RfdUtil.getFastestGpsTime(protoObj.getRfdList());
            long latestGpstime = RfdUtil.getLatestGpsTime(null, protoObj.getRfdList());

            //redis seq, detectedId 초기화. 기존 sequence 및 properties를 삭제한다.
            String pathsArryKeyName = RfdUtil.getPathsArryRedisKey(vehicleId);
            String gpsSeqKeyName = RfdUtil.getGpsSeqRedisKey(vehicleId);
            String laneDetectedIdKeyName = RfdUtil.getLaneDetectedIdRedisKey(vehicleId);
            String localiDetectedIdKeyName = RfdUtil.getLocalizationDetectedIdRedisKey(vehicleId);
            redisDao.delPropertie(pathsArryKeyName, pathSeqKeyName, gpsSeqKeyName, laneDetectedIdKeyName, localiDetectedIdKeyName);

            //새로운 경로정보를 생성.
            pathInfo = new PathInfo();
            String pathId = vehicleId + "_" + fastestGpsTime;
            pathInfo.setPathSeq(redisDao.incr(pathSeqKeyName));
            pathInfo.setPathId(pathId);
            pathInfo.setFastestGpsTime(fastestGpsTime);
            pathInfo.setLatestGpsTime(latestGpstime);
        } else {
            // 기존 경로정보 업데이트
            long beforeLastestGpsTime = pathInfo.getLatestGpsTime();
            long latestGpstime = RfdUtil.getLatestGpsTime(beforeLastestGpsTime, protoObj.getRfdList());
            pathInfo.setPathSeq(redisDao.incr(pathSeqKeyName)); //주행경로내 구간순번. 지난 값에서 1을 증가시킨다.
//            pathInfo.setFastestGpsTime(pathInfo.getFastestGpsTime()); //정보 유지
            pathInfo.setLatestGpsTime(latestGpstime); //Timeout 확인 위한 가장 최근 gpsTime set
        }
        return pathInfo;
    }

    /**
     * byte[] -> protobuf object -> json -> spark sql -> explode -> java object -> json -> kafka 비효율적이다.
     */
    @Test
    public void structuredStreamWithProtobufTest() {
        String appName = "SparkStructuredStreamForKafkaTest";
        SparkSession spark = SparkSession
                .builder()
                .master("local[2]")
                .config("spark.network.timeout", 400000) // for debug
                .config("spark.executor.heartbeatInterval", 300000) // for debug.
                .appName(appName)
                .getOrCreate();

        // byte[] -> json String 으로 변환.
        StructType dataType = new StructType(new StructField[] {new StructField("value", DataTypes.StringType, true, Metadata.empty())});
        spark.udf().register("deserializer", (byte[] data) -> {
            if(data == null) {
                return null;
            }
            String jsonValue = null;
            try {
                Parser<RFDProto20180601_2.SendData> parser = RFDProto20180601_2.SendData.parser();
                Message sendData  = parser.parseFrom(data);
                jsonValue = JsonFormat.printer().print(sendData.toBuilder());
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
            return RowFactory.create(jsonValue);
        }, dataType);

        Dataset<Row> kafkaSource = spark
                .readStream()
                .format("kafka")
                .option("kafka.bootstrap.servers", "localhost:9092")
                .option("subscribe", "ames_test")
                .option("startingOffsets", "latest") // earliest, latest
                .option("maxOffsetsPerTrigger", 1000)
                .load()
                ;

        kafkaSource.printSchema();
        try {
            Dataset<Row> allJson = kafkaSource
                    .select("value").as(Encoders.BINARY())
                            // byte[] -> json String 으로 변환.
                    .selectExpr("deserializer(value) as rows")
                    .select("rows.*");

            allJson.printSchema();
            allJson.writeStream()
                    .format("console")
                    .trigger(Trigger.ProcessingTime("5 seconds"))
                    .start();

            Dataset<Row> jsonAllString = allJson.select(from_json(col("value"), JsonDFSchema.sendData()).as("values")).select("values.*");

            jsonAllString.printSchema();

            Dataset<Row> lvl1 = jsonAllString                               //withColumn은 기존컬럼을 변경하거나, 없으면 추가한다.
                    .withColumn("rfd", explode(col("rfd")))              //array타입 rfd를 explode
                    .withColumn("rfd.lane", explode(col("rfd.lane")))  //array타입 lane를 explode
                    .withColumn("rfd.rm", explode(col("rfd.rm")))       //array타입 rm를 explode
                    .withColumn("rfd.rs", explode(col("rfd.rs")))       //array타입 rs를 explode
                    .withColumn("rfd.ts", explode(col("rfd.ts")))       //array타입 ts를 explode
                    .withColumn("rfd.tl", explode(col("rfd.tl")))       //array타입 tl를 explode
                    .select(
                            col("createTime").as("createTime")
                            ,col("vehicleInfo.vehicleId").as("vehicleInfo.vehicleId")
                            ,col("vehicleInfo.length").as("vehicleInfo.length")
                            ,col("vehicleInfo.height").as("vehicleInfo.height")
                            ,col("vehicleInfo.width").as("vehicleInfo.width")
                            ,col("cameraInfo.type").as("cameraInfo.type")
                            ,col("cameraInfo.x").as("cameraInfo.x")
                            ,col("cameraInfo.y").as("cameraInfo.y")
                            ,col("cameraInfo.z").as("cameraInfo.z")
                            ,col("rfd.gps.time").as("rfd.gps.time")
                            ,col("rfd.gps.lat").as("rfd.gps.lat")
                            ,col("rfd.gps.lon").as("rfd.gps.lon")
                            ,col("rfd.gps.alt").as("rfd.gps.alt")
                            ,col("rfd.gps.heading").as("rfd.gps.heading")
                            ,col("rfd.gps.speed").as("rfd.gps.speed")
                            ,col("rfd.gps.driveLane").as("rfd.gps.driveLane")
                            ,col("rfd.lane.type").as("rfd.lane.type")
                            ,col("rfd.lane.color").as("rfd.lane.color")
                            ,col("rfd.lane.pos").as("rfd.lane.pos")
                            ,col("rfd.rm.type").as("rfd.rm.type")
                            ,col("rfd.rm.x").as("rfd.rm.x")
                            ,col("rfd.rm.y").as("rfd.rm.y")
                            ,col("rfd.rm.width").as("rfd.rm.width")
                            ,col("rfd.rm.height").as("rfd.rm.height")
                            ,col("rfd.rs.type").as("rfd.rs.type")
                            ,col("rfd.rs.x").as("rfd.rs.x")
                            ,col("rfd.rs.y").as("rfd.rs.y")
                            ,col("rfd.rs.width").as("rfd.rs.width")
                            ,col("rfd.rs.height").as("rfd.rs.height")
                            ,col("rfd.ts.type").as("rfd.ts.type")
                            ,col("rfd.ts.x").as("rfd.ts.x")
                            ,col("rfd.ts.y").as("rfd.ts.y")
                            ,col("rfd.ts.width").as("rfd.ts.width")
                            ,col("rfd.ts.height").as("rfd.ts.height")
                            ,col("rfd.tl.type").as("rfd.tl.type")
                            ,col("rfd.tl.x").as("rfd.tl.x")
                            ,col("rfd.tl.y").as("rfd.tl.y")
                            ,col("rfd.tl.width").as("rfd.tl.width")
                            ,col("rfd.tl.height").as("rfd.tl.height")
                    );
            lvl1.printSchema();
            lvl1.writeStream()
                    .foreach(new BasicForeachWriter())
//                    .format("console")
                    .trigger(Trigger.ProcessingTime("5 seconds"))
                    .start().awaitTermination();

        } catch (StreamingQueryException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void writeByCaseTest() {
        String appName = "SparkStructuredStreamForKafkaTest";
        SparkSession spark = SparkSession
                .builder()
                .master("local[2]")
                .config("spark.network.timeout", 400000) // for debug
                .config("spark.executor.heartbeatInterval", 300000) // for debug.
                .appName(appName)
                .getOrCreate();

        Dataset<Row> kafkaSource = spark
                .readStream()
                .format("kafka")
                .option("kafka.bootstrap.servers", "localhost:9092")
                .option("subscribe", "ames_test")
                .option("startingOffsets", "latest") // earliest, latest
                .option("maxOffsetsPerTrigger", 1000)
                .load()
                ;

        kafkaSource.printSchema();
        try {
            //1. raw data -> hdfs
            Dataset<Row> dsRaw = kafkaSource.select("value");
            dsRaw.printSchema(); // debug
            dsRaw.writeStream()
                    .format("console")
                    .trigger(Trigger.ProcessingTime("5 seconds"))
//                    .start().awaitTermination();
                    .start();


            //2. raw -> json string
            //  - 기본값
            Dataset<Row> dsRawToJson = dsRaw.selectExpr("deserializer(value) as rows").select("rows.*");
            dsRawToJson.printSchema(); //debug
            dsRawToJson.writeStream()
                    .format("console")
                    .trigger(Trigger.ProcessingTime("5 seconds"))
                    .start().awaitTermination();

            //3. json string explode.
            Dataset<Row> dsExplode = dsRawToJson
                    .withColumn("rfd", explode(col("rfd")))              //array타입 rfd를 explode
                    .withColumn("rfd.lane", explode(col("rfd.lane")))  //array타입 lane를 explode
                    .withColumn("rfd.rm", explode(col("rfd.rm")))       //array타입 rm를 explode
                    .withColumn("rfd.rs", explode(col("rfd.rs")))       //array타입 rs를 explode
                    .withColumn("rfd.ts", explode(col("rfd.ts")))       //array타입 ts를 explode
                    .withColumn("rfd.tl", explode(col("rfd.tl")))       //array타입 tl를 explode
                    ;
            dsExplode.printSchema();
            dsExplode.writeStream()
                    .format("console")
                    .trigger(Trigger.ProcessingTime("5 seconds"))
                    .start().awaitTermination();


            //4. 각 파트별 json data 생성
            //  1). ETN_RFD_DRIVE_PATH : 주행 최초 insert, 최후 update 수행.
            //    * redis 에 vehicle_id 로 set을 조회하여 존재여부 확인 필요.
            //      - 존재하지 않을 시, 신규 주행 정보 기록.
            //    * 필드 정보
            //    (I)PATH_ID	            String	주행 ID -> vehicle_id + gps_time
            //    (I)PATH_NAME	            String	주행 이름 -> path_id
            //    (U)PATH_XYZM	            String	주행 좌표 (Geometry) -> LINESTRING(1 0, 0 1, -1 0) 형식
            //    (U)PATH_END_FLAG	        String	주행 종료 여부 -> ??
            //    (I)PATH_START_DATETIM	long	주행 시작 시간 -> 최초 gps_time
            //    (U)PATH_END_DATETIME	    long	주행 종료 시간 -> 최후 gps_time



        } catch (StreamingQueryException e) {
            e.printStackTrace();
        }
    }

    public static UserDefinedFunction bytesToJson(Parser<Message> parser, DataType dataType) {
        return functions.udf(
                (byte[] data) -> {
                    if(data == null) {
                        return null;
                    }
                    String jsonValue = null;
                    try {
                        Message sendData  = parser.parseFrom(data);
                        jsonValue = JsonFormat.printer().print(sendData.toBuilder());
                    } catch (InvalidProtocolBufferException e) {
                        e.printStackTrace();
                    }
                    return RowFactory.create(jsonValue); //byte[] -> String
                }, dataType);
    }

    /**
     * json 문자열을 인식하는것은 사실상 변환이 필요 없다..
     * @param parser
     * @param dataType
     * @return
     */
    public static UserDefinedFunction strToJson(Parser<Message> parser, DataType dataType) {
        return functions.udf(
                (String data) -> {
                    return RowFactory.create(data);
                }, dataType);
    }
}
