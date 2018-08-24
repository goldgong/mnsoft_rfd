package com.mnsoft.mac.rfd.app;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Parser;
import com.mnsoft.mac.rfd.common.dto.*;
import com.mnsoft.mac.rfd.app.redis.PathInfo;
import com.mnsoft.mac.rfd.common.kafka.KafkaFactory;
import com.mnsoft.mac.rfd.common.kafka.KafkaSender;
import com.mnsoft.mac.rfd.common.util.JsonUtil;
import com.mnsoft.mac.rfd.app.redis.PathPartInfo;
import com.mnsoft.mac.rfd.app.redis.RedisDao;
import com.mnsoft.mac.rfd.app.redis.RedisSyncClientWrapper;
import com.mnsoft.mac.rfd.app.util.RfdUtil;
import com.mnsoft.mac.rfd.protobuff.RFDProto20180601_2;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.spark.SparkConf;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.ConsumerStrategies;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.LocationStrategies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;

import java.util.*;

/**
 * Created by goldgong on 2018-08-22.
 */
public class SparkDStreamWithKafka {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private Config config;

    public SparkDStreamWithKafka(Config config) {
        this.config = config;
    }

    public void doProcess() {
        SparkConf conf = new SparkConf();
        conf.setAppName(config.getString(ConfigKey.appName.value()));
        conf.setMaster(config.getString(ConfigKey.sparkMaster.value()));
//        conf.set("spark.hadoop.cloneConf", "true");

        String checkPointPath = config.getString(ConfigKey.sparkStreamCheckPointPath.value());
        String externalKafkaTopic = config.getString(ConfigKey.externalKafkaTopic.value());

        long batchDuration = 5000;

        //5초 간격으로 spark DStream 수행
        JavaStreamingContext javaStreamingContext = new JavaStreamingContext(conf, new Duration(batchDuration));

        Map<String, Object> kafkaParams = new HashMap<>();
        kafkaParams.put("bootstrap.servers", config.getString(ConfigKey.externalKafkaBootstrapServers.value()));
        kafkaParams.put("key.deserializer", config.getString(ConfigKey.externalKafkaKeyDeserializer.value()));
        kafkaParams.put("value.deserializer", config.getString(ConfigKey.externalKafkaValueDeserializer.value()));
        kafkaParams.put("group.id", config.getString(ConfigKey.externalKafkaGroupId.value()));
        kafkaParams.put("auto.offset.reset", config.getString(ConfigKey.externalKafkaAutoOffsetReset.value()));
        kafkaParams.put("enable.auto.commit", config.getBoolean(ConfigKey.externalKafkaEnableAutoCommit.value()));

        Collection<String> topics = Arrays.asList(externalKafkaTopic);
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
        RedisSyncClientWrapper redisClient =
                new RedisSyncClientWrapper(
                        config.getString(ConfigKey.redisHost.value())
                        , config.getInt(ConfigKey.redisPort.value())
                        , config.getInt(ConfigKey.redisTimeout.value())
                        , config.getBoolean(ConfigKey.redisStandalone.value()));

        RedisDao redisDao = new RedisDao(redisClient);
        KafkaSender kafkaSender =
                KafkaFactory.getTextSenderInstance(
                        config.getString(ConfigKey.internalKafkaBootstrapServers.value())
                        , config.getString(ConfigKey.internalKafkaTopic.value()));

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
}
