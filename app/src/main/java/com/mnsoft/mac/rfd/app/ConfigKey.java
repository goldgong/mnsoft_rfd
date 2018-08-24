package com.mnsoft.mac.rfd.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by goldgong on 2018-06-25.
 */
public enum ConfigKey {

    appName("rfd.spark.app-name")
    ,sparkMaster("rfd.spark.master")
    ,sparkStreamCheckPointPath("rfd.spark.stream.check-point-path")
    ,sparkStreamBatchDuration("rfd.spark.stream.batch-duration")

    ,externalKafkaBootstrapServers("rfd.external-kafka.bootstrap-servers")
    ,externalKafkaGroupId("rfd.external-kafka.group-id")
    ,externalKafkaTopic("rfd.external-kafka.topic")
    ,externalKafkaKeyDeserializer("rfd.external-kafka.key-deserializer")
    ,externalKafkaValueDeserializer("rfd.external-kafka.value-deserializer")
    ,externalKafkaAutoOffsetReset("rfd.external-kafka.auto-offset-reset")
    ,externalKafkaEnableAutoCommit("rfd.external-kafka.enable-auto-commit")

    ,internalKafkaBootstrapServers("rfd.internal-kafka.bootstrap-servers")
    ,internalKafkaGroupId("rfd.internal-kafka.group-id")
    ,internalKafkaTopic("rfd.internal-kafka.topic")
    ,internalKafkaKeyDeserializer("rfd.internal-kafka.key-deserializer")
    ,internalKafkaValueDeserializer("rfd.internal-kafka.value-deserializer")
    ,internalKafkaAutoOffsetReset("rfd.internal-kafka.auto-offset-reset")
    ,internalKafkaEnableAutoCommit("rfd.internal-kafka.enable-auto-commit")

    ,redisHost("rfd.redis.host")
    ,redisPort("rfd.redis.port")
    ,redisTimeout("rfd.redis.timeout")
    ,redisStandalone("rfd.redis.standalone");

    private String value;
    ConfigKey(String value) {
        this.value = value;
    }
    public String value() {
        return this.value;
    }
}
