package com.mnsoft.mac.rfd.app.kafka;

import com.google.common.collect.Lists;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Parser;
import com.google.protobuf.util.JsonFormat;
import com.mnsoft.mac.rfd.common.kafka.KafkaFactory;
import com.mnsoft.mac.rfd.common.kafka.KafkaReceiver;
import com.mnsoft.mac.rfd.common.kafka.KafkaSender;
import com.mnsoft.mac.rfd.common.kafka.TextReceiver;
import com.mnsoft.mac.rfd.protobuff.RFDProto20180601_2;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * 주지 사항
 *   - 각 타입별 default값은 proto binary, toString 내용에 포함되지 않는다.
 *     . 예) Enum 타입의 값이 0이면 proto binary 파일에 기록되지 않는다.
 * Created by goldgong on 2018-06-20.
 */
public class KafkaRfdTest {
    class ThreadRunner {
        private Runnable runnable;
        public ThreadRunner(Runnable runnable) {
            this.runnable = runnable;
        }

        public void doWork() {
            Thread run = new Thread(this.runnable);
            run.start();
            try {
                run.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void kafkaRfdRecvTest() {
        ProtoReceiver protoReceiver = new ProtoReceiver<RFDProto20180601_2.SendData>();
        protoReceiver.setParser(RFDProto20180601_2.SendData.parser());

//        KafkaReceiver kafkaReceiver = KafkaFactory.getProtobufReceiverInstance("192.168.203.101:9092,192.168.203.105:9092", "ames_test", protoReceiver);
        KafkaReceiver kafkaReceiver = KafkaFactory.getProtobufReceiverInstance("localhost:9092", "external-rfd", protoReceiver);
//        KafkaReceiver kafkaReceiver = KafkaFactory.getProtobufReceiverInstance("server01:9092", "ames_test", protoReceiver);
        ThreadRunner runner = new ThreadRunner(kafkaReceiver);
        runner.doWork();
    }

    @Test
    public void kafkaJsonRecvTest() {
        TextReceiver textReceiver = new TextReceiver();
//        KafkaReceiver kafkaReceiver = KafkaFactory.getTextReceiverInstance("localhost:9092", "internal-rfd", textReceiver);
        KafkaReceiver kafkaReceiver = KafkaFactory.getTextReceiverInstance("172.27.0.65:9092,172.27.0.128:9092,172.27.0.207:9092", "internal-rfd", textReceiver);
//        KafkaReceiver kafkaReceiver = KafkaFactory.getProtobufReceiverInstance("server01:9092", "ames_test", protoReceiver);
        ThreadRunner runner = new ThreadRunner(kafkaReceiver);
        runner.doWork();
    }

    @Test
    public void kafkaRfdSendTest() {
        RFDProto20180601_2.SendData sendData = getRFDProto20180601_2SendData();
//        KafkaSender kafkaSender = KafkaFactory.getProtobufSenderInstance("192.168.203.101:9092,192.168.203.105:9092", "ames_test");
//        KafkaSender kafkaSender = KafkaFactory.getProtobufSenderInstance("localhost:9092", "external-rfd");
        KafkaSender kafkaSender = KafkaFactory.getProtobufSenderInstance("172.27.0.65:9092,172.27.0.128:9092,172.27.0.207:9092", "external-rfd");
//        KafkaSender kafkaSender = KafkaFactory.getProtobufSenderInstance("server01:9092", "ames_test");
        byte[] sendArry = sendData.toByteArray();
        System.out.println("protobuf bytearry:" + sendArry);
        System.out.println("protobuf length:" + sendArry.length);
        try {
            String jsonValue = JsonFormat.printer().print(sendData.toBuilder());
            System.out.println("json length:" + jsonValue.getBytes().length);
            System.out.println("json value:" + jsonValue);

        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

        kafkaSender.sendMessage(sendArry);
    }

    @Test
    public void kafkaRfdBinarySendTest() throws IOException {
        String[] bDataList = {"RFD_20180720_145700_PACKET_PROTO.data"
                ,"RFD_20180720_145847_PACKET_PROTO.data"
                ,"RFD_20180720_150037_PACKET_PROTO.data"
                ,"RFD_20180720_150236_PACKET_PROTO.data"
                ,"RFD_20180720_150422_PACKET_PROTO.data"
                ,"RFD_20180720_150636_PACKET_PROTO.data"
                ,"RFD_20180720_150827_PACKET_PROTO.data"
                ,"RFD_20180720_151022_PACKET_PROTO.data"};

        RFDProto20180601_2.SendData sendData = getRFDProto20180601_2SendData();
        KafkaSender kafkaSender = KafkaFactory.getProtobufSenderInstance("localhost:9092", "external-rfd");

        for(String fileNm : bDataList) {
            byte[] sendArry = IOUtils.toByteArray(getClass().getResourceAsStream("/protobuf/binary/" + fileNm));

            try {
                String jsonValue = JsonFormat.printer().print(sendData.toBuilder());
                System.out.println("json length:" + jsonValue.getBytes().length);
                System.out.println("json value:" + jsonValue);

            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
            kafkaSender.sendMessage(sendArry);
            System.out.println("protobuf bytearry:" + sendArry);
            System.out.println("protobuf length:" + sendArry.length);
        }
    }

    /**
     * 버전이 맞지 않아 spark-kafka 수신시 정상파싱 안됨..
     * @throws IOException
     */
    @Test
    public void transformRfdBinaryTest() throws IOException {
        Parser<RFDProto20180601_2.SendData> parser = RFDProto20180601_2.SendData.parser();

        String[] bDataList = {"RFD_20180720_145700_PACKET_PROTO.data"
                            ,"RFD_20180720_145847_PACKET_PROTO.data"
                            ,"RFD_20180720_150037_PACKET_PROTO.data"
                            ,"RFD_20180720_150236_PACKET_PROTO.data"
                            ,"RFD_20180720_150422_PACKET_PROTO.data"
                            ,"RFD_20180720_150636_PACKET_PROTO.data"
                            ,"RFD_20180720_150827_PACKET_PROTO.data"
                            ,"RFD_20180720_151022_PACKET_PROTO.data"};
        String outDirPath = "D:/dev/git/mnsoft-mac/rfd/rfd-sinker/spark-kafka-sinker/sample";

        for(String fileNm : bDataList) {
            byte[] data = IOUtils.toByteArray(getClass().getResourceAsStream("/protobuf/binary/" + fileNm));
            RFDProto20180601_2.SendData protoObj  = parser.parseFrom(data);

            //1. object -> string (객체 매핑된 string으로 출력)
            String printStr = protoObj.toString();
            FileUtils.writeStringToFile(new File(outDirPath + "/" + fileNm + ".string"), printStr, false);

            //2. object -> json (객체 매핑 안된 json으로 출력)
            String jsonValue = JsonFormat.printer().print(protoObj.toBuilder());
            FileUtils.writeStringToFile(new File(outDirPath + "/" + fileNm + ".json"), jsonValue, false);
            System.out.println("[" + fileNm + "]Received message: " + jsonValue);
//            System.out.println("lane.type:" + protoObj.getRfd(0).getLane(0).getTypeValue());

            //3. json -> object -> string (객체 매핑된 string으로 출력)
            RFDProto20180601_2.SendData.Builder newBuilder = RFDProto20180601_2.SendData.newBuilder();
            JsonFormat.parser().merge(jsonValue, newBuilder);
            FileUtils.writeStringToFile(new File(outDirPath + "/" + fileNm + ".jsonToString"), newBuilder.build().toString(), false);
        }
    }

    @Test
    public void binarySampleWriteFile() {
        String outDirPath = "D:/dev/git/skt_tdna/src/infra-rfd/sample";

        RFDProto20180601_2.SendData sendData = getRFDProto20180601_2SendData();
        byte[] sendArry = sendData.toByteArray();
        try {
            FileOutputStream fos = new FileOutputStream(outDirPath+"/sample_binary.dat");
            fos.write(sendArry);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public RFDProto20180601_2.SendData getRFDProto20180601_2SendData() {
        RFDProto20180601_2.SendData sendData =
                RFDProto20180601_2.SendData.newBuilder()
                        .setVehicleInfo(getVehicleInfo())
                        .setCameraInfo(getCameraInfo())
                        .addAllRfd(getRfdList(1))
                        .addAllRfd(getRfdList(2))
                        .build();
        return sendData;
    }

    private Iterable<? extends RFDProto20180601_2.RFD> getRfdList(int weighted) {
        List<RFDProto20180601_2.RFD> rfdList = Lists.newArrayList();
        RFDProto20180601_2.RFD rfdData =
                RFDProto20180601_2.RFD.newBuilder()
                        .setGps(getGpsInfo(weighted))
                        .addLane(getLaneInfo(weighted, 1))
                        .addLane(getLaneInfo(weighted, 2))
                        .addRm(getRoadMark(weighted, 1))
                        .addRm(getRoadMark(weighted, 2))
                        .addRs(getRoadSine(weighted, 1))
                        .addRs(getRoadSine(weighted, 2))
                        .addTs(getTrafficSign(weighted, 1))
                        .addTs(getTrafficSign(weighted, 2))
                        .addTl(getTrafficLight(weighted, 1))
                        .addTl(getTrafficLight(weighted, 2))
                        .build();
        rfdList.add(rfdData);
        return rfdList;
    }

    private RFDProto20180601_2.TrafficLight getTrafficLight(int weighted, int weighted2) {
        RFDProto20180601_2.TrafficLight trafficLight =
                RFDProto20180601_2.TrafficLight.newBuilder()
                        .setType(RFDProto20180601_2.TrafficLight.Type.TL_000)
                        .setX(111 + weighted + weighted2)
                        .setY(222 + weighted + weighted2)
                        .setWidth(100 + weighted + weighted2)
                        .setHeight(200 + weighted + weighted2)
                        .build();
        return trafficLight;
    }

    private RFDProto20180601_2.TrafficSign getTrafficSign(int weighted, int weighted2) {
        RFDProto20180601_2.TrafficSign trafficSign =
                RFDProto20180601_2.TrafficSign.newBuilder()
                        .setType(RFDProto20180601_2.TrafficSign.Type.TS_000)
                        .setX(111 + weighted + weighted2)
                        .setY(222 + weighted + weighted2)
                        .setWidth(100 + weighted + weighted2)
                        .setHeight(200 + weighted + weighted2)
                        .build();
        return trafficSign;
    }

    private RFDProto20180601_2.RoadSign getRoadSine(int weighted, int weighted2) {
        RFDProto20180601_2.RoadSign roadSign =
                RFDProto20180601_2.RoadSign.newBuilder()
                        .setType(RFDProto20180601_2.RoadSign.Type.RS_000)
                        .setX(111 + weighted + weighted2)
                        .setY(222 + weighted + weighted2)
                        .setWidth(100 + weighted + weighted2)
                        .setHeight(200 + weighted + weighted2)
                        .build();
        return roadSign;
    }

    private RFDProto20180601_2.RoadMark getRoadMark(int weighted, int weighted2) {
        RFDProto20180601_2.RoadMark roadMark =
                RFDProto20180601_2.RoadMark.newBuilder()
                        .setType(RFDProto20180601_2.RoadMark.Type.RM_000)
                        .setX(111 + weighted + weighted2)
                        .setY(222 + weighted + weighted2)
                        .setWidth(100 + weighted + weighted2)
                        .setHeight(200 + weighted + weighted2)
                        .build();

        return roadMark;
    }

    private RFDProto20180601_2.Lane getLaneInfo(int weighted, int weighted2) {
        int lnNumber = RFDProto20180601_2.Lane.LaneType.LN_000_VALUE;
        RFDProto20180601_2.Lane laneInfo =
                RFDProto20180601_2.Lane.newBuilder()
                        .setType(RFDProto20180601_2.Lane.LaneType.LN_000)
                        .setColor(RFDProto20180601_2.Lane.LaneColor.LC_000)
                        .setPos(333 + weighted + weighted2)
                        .build();
        return laneInfo;
    }


    private RFDProto20180601_2.GPSInfo getGpsInfo(int weighted) {
        RFDProto20180601_2.GPSInfo gpsInfo =
                RFDProto20180601_2.GPSInfo.newBuilder()
                        .setTime(180101125059999l + weighted)
                        .setLat(10l*360000l*10l + weighted)
                        .setLon(20l*360000l*10l + weighted)
                        .setAlt(1000 + weighted)
                        .setHeading(10*10)
                        .setSpeed(20*10)
                        .setDriveLane(3)
                        .build();

        return gpsInfo;
    }

    private RFDProto20180601_2.CameraInfo getCameraInfo() {
        RFDProto20180601_2.CameraInfo cameraInfo =
                RFDProto20180601_2.CameraInfo.newBuilder()
                        .setType(RFDProto20180601_2.CameraInfo.Type.CM_000)
                        .setX(111)
                        .setY(222)
                        .setZ(333)
                        .build();
        return cameraInfo;
    }

    private RFDProto20180601_2.VehicleInfo getVehicleInfo() {
//        ByteString vehicleId = null;
        int vehicleId;
//        try {
//            vehicleId = ByteString.copyFrom("vehicle_id_001","utf-8");
            vehicleId = 1;
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
        RFDProto20180601_2.VehicleInfo vehicleInfo =
                RFDProto20180601_2.VehicleInfo.newBuilder()
                        .setVehicleId(vehicleId)
                        .setLength(100)
                        .setHeight(50)
                        .setWidth(40)
                        .build();
        return vehicleInfo;
    }

}
