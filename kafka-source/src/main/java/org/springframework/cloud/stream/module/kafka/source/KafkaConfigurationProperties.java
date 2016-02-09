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
 * Properties related to connecting to Kafka broker.
 *
 * @author Soby Chacko
 */
@ConfigurationProperties
public class KafkaConfigurationProperties {

    private String zkConnect = "localhost:2181";

    private String zkConnectionTimeout = "6000";

    private String zkSessionTimeout = "6000";

    private String zkSyncTime = "2000";

    private Integer concurrency = 1;

    private Integer maxFetch = 1024 * 1024;

    private Integer stopTimeout = 1000;

    private Integer queueSize = 1024;

    private Class keyDecoder;

    private Class payloadDecoder;

    private Map<String, String> partitions = new HashMap<>();

    private Map<String, Map<Integer, Long>> initialOffsets = new HashMap<>();

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

    public Class getKeyDecoder() {
        return keyDecoder;
    }

    public void setKeyDecoder(Class keyDecoder) {
        this.keyDecoder = keyDecoder;
    }

    public Class getPayloadDecoder() {
        return payloadDecoder;
    }

    public void setPayloadDecoder(Class payloadDecoder) {
        this.payloadDecoder = payloadDecoder;
    }

    public Map<String, Map<Integer, Long>> getInitialOffsets() {
        return initialOffsets;
    }

    public void setInitialOffsets(Map<String, Map<Integer, Long>> initialOffsets) {
        this.initialOffsets = initialOffsets;
    }

}
