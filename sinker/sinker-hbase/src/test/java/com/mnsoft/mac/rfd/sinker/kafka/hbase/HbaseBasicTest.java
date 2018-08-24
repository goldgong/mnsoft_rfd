package com.mnsoft.mac.rfd.sinker.kafka.hbase;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Parser;
import com.mnsoft.mac.rfd.protobuff.RFDProto20180601_2;
import com.mnsoft.mac.rfd.sinker.hbase.util.PhoenixJDBCHelper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by goldgong on 2018-08-06.
 */
public class HbaseBasicTest {

    @Test
    public void hbaseBasicTest() {
        //Step 1:Instantiate the Configuration Class
        Configuration config = HBaseConfiguration.create();
        config.set("hbase.zookeeper.quorum", "server01,server02");
//        config.set("hbase.zookeeper.quorum", "server01");
//        config.set("hbase.rpc.timeout", ""); //how long an RPC call can run before it times out
//        config.set("hbase.rpc.read.timeout", "");
//        config.set("hbase.rpc.write.timeout", "");
//        config.set("hbase.client.operation.timeout", "");
        try {
            //1. hbase : create 'KYJTEST6', 'F1'
            //2. phoenix : create table if not exists "KYJTEST6" (ID1 varchar, "F1".VAL1 varchar, "F1".BINARY1 varbinary, constraint my_pk primary key (ID1))
            //phoenix : create table if not exists "KYJTEST6" (ID1 varchar primary key, "F1".VAL1 varchar)
            String tablename = "KYJTEST6";
            try (Connection connection = ConnectionFactory.createConnection(config);
                 //Step 2:Instantiate the Table Class
                 Table table = connection.getTable(TableName.valueOf(tablename))) {
                // use table as needed, the table returned is lightweight

                //Step 3: Instantiate the PutClass
                // \x00 은 phoenix 의 복합 pk 필드 구분자
//                Put p = new Put(Bytes.toBytes("pk1\\x00pk2"));
                Put p = new Put(Bytes.toBytes("pk3"));

                //Step 4: Insert Data
                //protobuf binary data를 hbase 에 put 한 후, phoenix에서 select 하여 가져온 후 역직렬 해보면 될 것.
//                KafkaRfdTest rfdTest = new KafkaRfdTest();
//                RFDProto20180601_2.SendData sendData = rfdTest.getRFDProto20180601_2SendData();
                byte[] data = IOUtils.toByteArray(getClass().getResourceAsStream("/src/test/resource/protobuf/binary/sample_binary.dat"));
//                byte[] sendArry = sendData.toByteArray();
                p.addColumn(Bytes.toBytes("F1"), Bytes.toBytes("VAL1"), Bytes.toBytes("var3_value"));
                p.addColumn(Bytes.toBytes("F1"), Bytes.toBytes("BINARY1"), data);

                //Step 5: Save the Data in Table
                table.put(p);

                //Step 6: Close the HTable Instance
                table.close();


            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @Test
    public void phoenixTest() {
        String jdbcUrl = "jdbc:phoenix:server01,server02:2181:/hbase";
        String driverName = "org.apache.phoenix.jdbc.PhoenixDriver";

        try {
            PhoenixJDBCHelper phoenixJDBCHelper = new PhoenixJDBCHelper(driverName, jdbcUrl);
            List<CharSequence> lists = phoenixJDBCHelper.selectListDataManager("select * from KYJTEST6");
//            upsert into kyjtest6 values ('pk2','val2');
            lists = lists;
            lists.forEach(line -> {
                String newLine = String.valueOf(line);
                String[] fields = StringUtils.split(newLine, '^');
                String binary = fields[2];
                System.out.println(binary);
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    /**
     * hbase binary put 후, 객체로 원복 테스트.
     *   - HBase 의 Bytes로 .fromHex 를 통해 헥사를 바이트로 변환 후, Protobuf Message 타입으로 역직렬을 수행한다.
     */
    @Test
    public void binaryToObjectTest() {

        String hexOfBinary="0a0808011064183220281208106f18de0120cd021a5c0a25089febd4fcd0f9281100000000882a81411900000000882a914120d00f28c80130c8013803120318cd021a0a106f18de01206428c801220a106f18de01206428c8012a0a106f18de01206428c801320a106f18de01206428c801";
        byte[] bytes = Bytes.fromHex(hexOfBinary);
        Parser<RFDProto20180601_2.SendData> parser = RFDProto20180601_2.SendData.parser();
        try {
            RFDProto20180601_2.SendData protoObj  = parser.parseFrom(bytes);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }
}

