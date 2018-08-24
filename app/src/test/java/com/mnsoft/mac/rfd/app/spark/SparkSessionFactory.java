package com.mnsoft.mac.rfd.app.spark;

import org.apache.spark.SparkConf;
import org.apache.spark.sql.SparkSession;

/**
 * Created by goldgong on 2018-06-29.
 */
public class SparkSessionFactory {

    public static SparkSession getOrCreateSession(String master, String appName, SparkConf conf) {
        SparkSession.Builder builder = SparkSession.builder();
        if(conf != null) {
            builder.config(conf);
        }

        if(master == null || master.startsWith("local")) {
            master = "local[2]";
        }

        builder.appName(appName).master(master);

        return builder.getOrCreate();
    }
}
