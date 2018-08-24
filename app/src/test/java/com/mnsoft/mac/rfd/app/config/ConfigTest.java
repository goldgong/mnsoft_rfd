package com.mnsoft.mac.rfd.app.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.Test;

/**
 * Created by goldgong on 2018-08-22.
 */
public class ConfigTest {

    @Test
    public void loadConfigTest() {
        String defaultConfig = "rfd-app-default.conf";
        Config rfdDefault = ConfigFactory.parseResources(defaultConfig);

        String modifyConfig = "rfd.conf";
        Config config = ConfigFactory.parseResources(modifyConfig).withFallback(rfdDefault);

        System.out.println(config.getString("rfd.spark.app-name"));
    }
}
