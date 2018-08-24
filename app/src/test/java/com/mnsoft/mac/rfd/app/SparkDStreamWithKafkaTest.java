package com.mnsoft.mac.rfd.app;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.Test;

/**
 * Created by goldgong on 2018-08-22.
 */
public class SparkDStreamWithKafkaTest {

    private Config initConfig() {
        String defaultConfig = "rfd-app-default.conf";
        Config rfdDefault = ConfigFactory.parseResources(defaultConfig);

        String modifyConfig = "rfd-app.conf";
        return ConfigFactory.parseResources(modifyConfig).withFallback(rfdDefault);
    }

    @Test
    public void sparkDStreamTest() {
        SparkDStreamWithKafka sparkDStreamWithKafka = new SparkDStreamWithKafka(initConfig());
        sparkDStreamWithKafka.doProcess();
    }
}
