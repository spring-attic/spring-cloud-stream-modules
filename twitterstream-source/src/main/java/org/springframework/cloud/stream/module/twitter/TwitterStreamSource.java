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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.Bindings;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.core.MessageProducer;
import org.springframework.social.twitter.api.impl.TwitterTemplate;

/**
 * TwitterStream source module.
 *
 * @author Ilayaperumal Gopinathan
 */
@EnableBinding(Source.class)
@EnableConfigurationProperties({TwitterCredentials.class, TwitterStreamProperties.class})
public class TwitterStreamSource {

	@Autowired
	TwitterCredentials credentials;

	@Autowired
	TwitterStreamProperties twitterStreamProperties;

	@Autowired
	@Bindings(TwitterStreamSource.class)
	Source source;

	@Bean
	public MessageProducer twitterStream(TwitterTemplate twitterTemplate) {
		TwitterStreamMessageProducer messageProducer =
				new TwitterStreamMessageProducer(twitterTemplate, twitterStreamProperties);
		messageProducer.setOutputChannel(source.output());
		return messageProducer;
	}

	@Bean
	public TwitterTemplate twitterTemplate() {
		return new TwitterTemplate(credentials.getConsumerKey(), credentials.getConsumerSecret(),
				credentials.getAccessToken(), credentials.getAccessTokenSecret());
	}
}
