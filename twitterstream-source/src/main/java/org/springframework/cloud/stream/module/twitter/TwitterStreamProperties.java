/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.cloud.stream.module.twitter;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Twitterstream source.
 *
 * @author Ilayaperumal Gopinathan
 */
@ConfigurationProperties
//todo: support more stream properties such as locations, track etc.,
public class TwitterStreamProperties {

	/**
	 * Twitter stream type (such as sample, firehose). Default is sample.
	 */
	private TwitterStreamType streamType = TwitterStreamType.SAMPLE;

	/**
	 * The language of the tweet text.
	 */
	private String language;

	public TwitterStreamType getStreamType() {
		return this.streamType;
	}

	public void setStreamType(TwitterStreamType streamType) {
		this.streamType = streamType;
	}

	public String getLanguage() {
		return this.language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}
}
