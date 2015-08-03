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

package org.springframework.cloud.stream.module.redis;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.stream.annotation.EnableModule;
import org.springframework.cloud.stream.annotation.Sink;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.integration.config.ConsumerEndpointFactoryBean;
import org.springframework.integration.redis.outbound.RedisPublishingMessageHandler;
import org.springframework.messaging.MessageHandler;

/**
 * A sink that can be used to insert data into a Redis store.
 *
 * @author Eric Bottard
 */
@EnableModule(Sink.class)
public class RedisSink {

	private static final Logger logger = LoggerFactory.getLogger(RedisSink.class);

	@Autowired
	private Sink sink;

	@Autowired
	@Qualifier("custom")
	private RedisConnectionFactory redisConnectionFactory;

	@Autowired
	private RedisSinkModuleOptions moduleOptions;

	@Bean
	public RedisSinkModuleOptions moduleOptions() {
		return new RedisSinkModuleOptions();
	}

	@Bean
	@ConditionalOnProperty("module.topicExpression")
	public MessageHandler topicExpressionMessageHandler() {
		RedisPublishingMessageHandler redisPublishingMessageHandler = new RedisPublishingMessageHandler(redisConnectionFactory);
		redisPublishingMessageHandler.setTopicExpression(new SpelExpressionParser().parseExpression(moduleOptions.getTopicExpression()));
		return redisPublishingMessageHandler;
	}

	@Bean
	public ConsumerEndpointFactoryBean consumerEndpointFactoryBean() {
		ConsumerEndpointFactoryBean consumerEndpointFactoryBean = new ConsumerEndpointFactoryBean();
		consumerEndpointFactoryBean.setInputChannel(sink.input());
		consumerEndpointFactoryBean.setHandler(topicExpressionMessageHandler());
		return consumerEndpointFactoryBean;
	}

}
