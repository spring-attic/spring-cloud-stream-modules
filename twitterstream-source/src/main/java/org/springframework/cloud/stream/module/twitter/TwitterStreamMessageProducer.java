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

import java.util.ArrayList;
import java.util.List;

import org.springframework.cloud.stream.messaging.Source;
import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.social.twitter.api.StreamDeleteEvent;
import org.springframework.social.twitter.api.StreamListener;
import org.springframework.social.twitter.api.StreamWarningEvent;
import org.springframework.social.twitter.api.Tweet;
import org.springframework.social.twitter.api.impl.TwitterTemplate;
import org.springframework.util.Assert;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *  {@link org.springframework.integration.core.MessageProducer} implementation to send Twitter stream messages.
 *
 * @author Ilayaperumal Gopinathan
 */
class TwitterStreamMessageProducer extends MessageProducerSupport {

	private final TwitterTemplate twitterTemplate;

	private final String streamType;

	private final ObjectMapper objectMapper = new ObjectMapper();

	TwitterStreamMessageProducer(TwitterTemplate twitterTemplate, String streamType) {
		Assert.notNull(twitterTemplate, "TwitterTemplate must not be null.");
		Assert.hasText(streamType, "Stream type must not be empty.");
		this.twitterTemplate = twitterTemplate;
		this.streamType = streamType;
	}

	@Override
	public void doStart() {
		List<StreamListener> listeners = new ArrayList<>();
		listeners.add(new StreamListener() {
			@Override
			public void onTweet(Tweet tweet) {
				try {
					//todo: Support conversion type; currently the tweet is written as JSON String value.
					sendMessage(MessageBuilder.withPayload(objectMapper.writeValueAsString(tweet)).build());
				}
				catch (JsonProcessingException e) {
					logger.debug("JSON processing exception while processing the tweet: " + e);
				}
			}

			@Override
			public void onDelete(StreamDeleteEvent deleteEvent) {
				//discard
			}

			@Override
			public void onLimit(int numberOfLimitedTweets) {
				logger.info("The stream is being track limited for " + numberOfLimitedTweets + " tweets.");
			}

			@Override
			public void onWarning(StreamWarningEvent warningEvent) {
				logger.error("Streaming source is in the danger of being disconnected.");
			}
		});
		if (this.streamType.equalsIgnoreCase(TwitterStreamProperties.FIREHOSE)) {
			twitterTemplate.streamingOperations().firehose(listeners);
		}
		else {
			twitterTemplate.streamingOperations().sample(listeners);
		}
	}

	@Override
	public void onInit() {
		setOutputChannel((MessageChannel) getBeanFactory().getBean(Source.OUTPUT));
		super.onInit();
	}
}
