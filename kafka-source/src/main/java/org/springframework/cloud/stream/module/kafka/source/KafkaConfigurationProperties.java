/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.stream.module.kafka.source;

import java.util.HashMap;
import java.util.Map;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

/**
 * Properties for the Kafka Source.
 *
 * @author Soby Chacko
 */
@ConfigurationProperties
public class KafkaConfigurationProperties {

    /**
     * Zookeeper connect string.
     */
    private String zkConnect = "localhost:2181";

    /**
     * Zookeeper connection timeout value.
     */
    private String zkConnectionTimeout = "6000";

    /**
     * Zookeeper session timeout value.
     */
    private String zkSessionTimeout = "6000";

    /**
     * Zookeeper sync time
     */
    private String zkSyncTime = "2000";

    /**
     * Concurrency attribute indicating the number of listeners over topic partitions.
     */
    private Integer concurrency = 1;

    /**
     * Max bytes that can be fetched by the consumer.
     */
    private Integer maxFetch = 1024 * 1024;

    /**
     * Timeout for waiting each concurrent consumer to finish before it is stopped.
     */
    private Integer stopTimeout = 1000;

    /**
     * The maximum number of messages that are buffered by each concurrent comsumer.
     */
    private Integer queueSize = 1024;

    /**
     * {@link kafka.serializer.Decoder} for Kafka topic Key.
     */
    private Class<?> keyDecoder;

    /**
     * {@link kafka.serializer.Decoder} for Kafka topic payload.
     */
    private Class<?> payloadDecoder;

    /**
     * Partitions to fetch from. For ex, partitions.foo=1,2,3 partitions.bar=0,1 etc.
     */
    private Map<String, String> partitions = new HashMap<>();

    /**
     * Initial offsets to read from the topic partitions.
     * For ex, initialOffsets.foo.0=0, initialOffsets.foo.1=10 etc.
     */
    private Map<String, Map<Integer, Long>> initialOffsets = new HashMap<>();

    /**
     * Comma separated list of Kafka topics.
     */
    private String[] topics;

    @AssertTrue(message = "Either a list of topics (--topics) OR partitions.<topic>=<comma separated partitions> (--partitions.<topic>) must be provided")
    public boolean isTopicsOrPartitions() {
        return !ObjectUtils.isEmpty(getTopics()) ^ !CollectionUtils.isEmpty(getPartitions());
    }

    @NotNull
    public int getQueueSize() {
        return queueSize;
    }

    public void setQueueSize(int queueSize) {
        this.queueSize = queueSize;
    }

    @NotNull
    public int getStopTimeout() {
        return stopTimeout;
    }

    public void setStopTimeout(int stopTimeout) {
        this.stopTimeout = stopTimeout;
    }

    @NotNull
    public int getMaxFetch() {
        return maxFetch;
    }

    public void setMaxFetch(int maxFetch) {
        this.maxFetch = maxFetch;
    }

    @NotNull
    public int getConcurrency() {
        return concurrency;
    }

    public void setConcurrency(int concurrency) {
        this.concurrency = concurrency;
    }

    public String[] getTopics() {
        return topics;
    }

    public void setTopics(String[] topics) {
        this.topics = topics;
    }

    public Map<String, String> getPartitions() {
        return partitions;
    }

    public void setPartitions(Map<String, String> partitions) {
        this.partitions = partitions;
    }

    @NotBlank
    public String getZkConnect() {
        return zkConnect;
    }

    public void setZkConnect(String zkConnect) {
        this.zkConnect = zkConnect;
    }

    @NotBlank
    public String getZkConnectionTimeout() {
        return zkConnectionTimeout;
    }

    public void setZkConnectionTimeout(String zkConnectionTimeout) {
        this.zkConnectionTimeout = zkConnectionTimeout;
    }

    @NotBlank
    public String getZkSessionTimeout() {
        return zkSessionTimeout;
    }

    public void setZkSessionTimeout(String zkSessionTimeout) {
        this.zkSessionTimeout = zkSessionTimeout;
    }

    @NotBlank
    public String getZkSyncTime() {
        return zkSyncTime;
    }

    public void setZkSyncTime(String zkSyncTime) {
        this.zkSyncTime = zkSyncTime;
    }

    public Class<?> getKeyDecoder() {
        return keyDecoder;
    }

    public void setKeyDecoder(Class<?> keyDecoder) {
        this.keyDecoder = keyDecoder;
    }

    public Class<?> getPayloadDecoder() {
        return payloadDecoder;
    }

    public void setPayloadDecoder(Class<?> payloadDecoder) {
        this.payloadDecoder = payloadDecoder;
    }

    public Map<String, Map<Integer, Long>> getInitialOffsets() {
        return initialOffsets;
    }

    public void setInitialOffsets(Map<String, Map<Integer, Long>> initialOffsets) {
        this.initialOffsets = initialOffsets;
    }

}
