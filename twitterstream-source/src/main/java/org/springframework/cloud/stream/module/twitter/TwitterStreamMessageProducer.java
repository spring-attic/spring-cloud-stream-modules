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

import java.net.URI;

import org.springframework.social.support.URIBuilder;
import org.springframework.social.twitter.api.impl.TwitterTemplate;
import org.springframework.util.StringUtils;

/**
 *  {@link org.springframework.integration.core.MessageProducer} implementation to send Twitter stream messages.
 *
 * @author Ilayaperumal Gopinathan
 */
class TwitterStreamMessageProducer extends AbstractTwitterInboundChannelAdapter {

	private static final String API_URL_BASE = "https://stream.twitter.com/1.1/statuses/";

	private final TwitterStreamProperties twitterStreamProperties;

	TwitterStreamMessageProducer(TwitterTemplate twitterTemplate, TwitterStreamProperties twitterStreamProperties) {
		super(twitterTemplate);
		this.twitterStreamProperties = twitterStreamProperties;
	}

	protected URI buildUri() {
		String path = this.twitterStreamProperties.getStreamType().equals(TwitterStreamType.FIREHOSE) ?
				"firehose.json" : "sample.json";
		URIBuilder b = URIBuilder.fromUri(API_URL_BASE + path);
		//todo: Support all the available properties
		if (StringUtils.hasText(this.twitterStreamProperties.getLanguage())) {
			b.queryParam("language", this.twitterStreamProperties.getLanguage());
		}
		return b.build();
	}

	protected void doSendLine(String line) {
		if (line.startsWith("{\"limit")) {
			logger.info("Twitter stream is being track limited.");
		} else if (line.startsWith("{\"delete")) {
			//discard
		} else if (line.startsWith("{\"warning")) {
			//discard
		}
		else {
			sendMessage(org.springframework.integration.support.MessageBuilder.withPayload(line).build());
		}
	}

}
