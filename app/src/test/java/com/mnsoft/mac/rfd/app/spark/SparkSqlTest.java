package com.mnsoft.mac.rfd.app.spark;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import com.mnsoft.mac.rfd.app.kafka.KafkaRfdTest;
import com.mnsoft.mac.rfd.protobuff.RFDProto20180601_2;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.*;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

/**
 * Created by goldgong on 2018-06-25.
 */
public class SparkSqlTest {

    @Test
    public void protobufJsonTest() {
        KafkaRfdTest testData = new KafkaRfdTest();
        RFDProto20180601_2.SendData sendData = testData.getRFDProto20180601_2SendData();
        String jsonValue = null;
        try {
            jsonValue = JsonFormat.printer().print(sendData.toBuilder());
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

        ObjectMapper mapper = new ObjectMapper();
        RFDProto20180601_2.SendData decode = null;
        JsonNode node = null;
        try {
//            decode = mapper.readValue(jsonValue, RFDProto20180601_2.SendData.class);

            node = mapper.readTree(jsonValue);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //=========================================================================

        String appName = "infra-rfd";
        String sparkMaster = "spark://192.168.203.101:7078";
        SparkConf conf = new SparkConf();
        conf.setAppName(appName);
        conf.setMaster(sparkMaster);
        conf.set("spark.hadoop.cloneConf", "true");
        conf.set("spark.driver.memory", "5g");
        conf.set("spark.driver.cores", "2");
        conf.set("spark.executor.memory", "2g");
        conf.set("spark.executor.cores", "2");
//        conf.set("spark.executor.instances", "1");

        SparkSession sparkSession = SparkSession.builder().config(conf).getOrCreate();

        JavaSparkContext jsc = new JavaSparkContext(sparkSession.sparkContext());




        List<String> listData = new ArrayList();
        System.out.println("node:"+node.toString());
        listData.add(node.toString());
//        listData.add(jsonValue);

//        JavaRDD rdd = jsc.parallelize(listData);
        SQLContext sqlContext = new SQLContext(jsc);
//        sqlContext.jsonRDD(rdd).registerTempTable("JsonTable");
        Dataset<Row> rdd2 = sqlContext.read().json("D:/dev/20180619_엠앤소프트_개발건/test.json");
        Dataset<Row> dataset = sqlContext.sql("select * from JsonTable");
        dataset.show();

    }
}
