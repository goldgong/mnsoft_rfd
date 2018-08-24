package com.mnsoft.mac.rfd.app.message;

import com.skt.ames.rfd.message.SampleProtos;
import org.junit.Test;

import java.io.*;

/**
 * Created by goldgong on 2018-06-20.
 */
public class ProtoSample {

    @Test
    public void pipedStreamTest() {
        SampleProtos.Sample sample =
                SampleProtos.Sample.newBuilder()
                        .setSample1("aaa")
                        .setSample2(123)
                        .setSample3("bbbb")
                        .build();


        PipedOutputStream pos = new PipedOutputStream();
        PipedInputStream pis = new PipedInputStream();
        try {
            pos.connect(pis);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            sample.writeTo(pos);
            pos.flush();
            pos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            SampleProtos.Sample sampleOut = SampleProtos.Sample.parseFrom(pis);
            System.out.println(sampleOut.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
