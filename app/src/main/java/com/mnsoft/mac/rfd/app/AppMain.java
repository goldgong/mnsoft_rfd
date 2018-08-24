package com.mnsoft.mac.rfd.app;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

/**
 * Created by goldgong on 2018-08-22.
 */
public class AppMain {

    private Config initConfig() {
        String defaultConfig = "rfd-app-default.conf";
        Config rfdDefault = ConfigFactory.parseResources(defaultConfig);

        String modifyConfig = "rfd-app.conf";
        return ConfigFactory.parseResources(modifyConfig).withFallback(rfdDefault);
    }

    private void doStart() {
        SparkDStreamWithKafka sparkDStreamWithKafka = new SparkDStreamWithKafka(initConfig());
        sparkDStreamWithKafka.doProcess();
    }

    public static void main(String[] args) {
        AppMain appMain = new AppMain();
        appMain.doStart();
    }
}
