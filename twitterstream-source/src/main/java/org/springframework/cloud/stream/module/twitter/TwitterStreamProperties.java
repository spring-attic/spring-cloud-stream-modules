/*
 * Copyright 2015 the original author or authors.
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
package org.springframework.cloud.stream.module.twitter;

import javax.validation.constraints.AssertTrue;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Twitterstream source.
 *
 * @author Ilayaperumal Gopinathan
 */
@ConfigurationProperties
public class TwitterStreamProperties {

	//todo: Support more stream types such as `filter`, `search` that TwitterTemplate implementation supports.
	protected static final String SAMPLE = "sample";

	protected static final String FIREHOSE = "firehose";

	private String streamType = SAMPLE;

	@AssertTrue(message = "Only `sample` or `firehose` stream operations are supported.")
	boolean isStreamTypeValid() {
		return (this.streamType.equalsIgnoreCase(SAMPLE) || this.streamType.equalsIgnoreCase(FIREHOSE));
	}

	public String getStreamType() {
		return this.streamType;
	}

	public void setStreamType(String streamType) {
		this.streamType = streamType;
	}
}
