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

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

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

	private String keyDecoder;

	private String payloadDecoder;

	private Map<String, String> partitions = new HashMap<>();

	private Map<String, Map<Integer, Long>> initialOffsets = new HashMap<>();

	private String[] topics;

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

	public String getZkConnect() {
		return zkConnect;
	}

	public void setZkConnect(String zkConnect) {
		this.zkConnect = zkConnect;
	}

	public String getZkConnectionTimeout() {
		return zkConnectionTimeout;
	}

	public void setZkConnectionTimeout(String zkConnectionTimeout) {
		this.zkConnectionTimeout = zkConnectionTimeout;
	}

	public String getZkSessionTimeout() {
		return zkSessionTimeout;
	}

	public void setZkSessionTimeout(String zkSessionTimeout) {
		this.zkSessionTimeout = zkSessionTimeout;
	}

	public String getZkSyncTime() {
		return zkSyncTime;
	}

	public void setZkSyncTime(String zkSyncTime) {
		this.zkSyncTime = zkSyncTime;
	}

	public String getKeyDecoder() {
		return keyDecoder;
	}

	public void setKeyDecoder(String keyDecoder) {
		this.keyDecoder = keyDecoder;
	}

	public String getPayloadDecoder() {
		return payloadDecoder;
	}

	public void setPayloadDecoder(String payloadDecoder) {
		this.payloadDecoder = payloadDecoder;
	}

	public Map<String, Map<Integer, Long>> getInitialOffsets() {
		return initialOffsets;
	}

	public void setInitialOffsets(Map<String, Map<Integer, Long>> initialOffsets) {
		this.initialOffsets = initialOffsets;
	}

}
