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
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.stream.annotation.EnableModule;
import org.springframework.cloud.stream.annotation.Sink;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.integration.config.ConsumerEndpointFactoryBean;
import org.springframework.integration.redis.config.RedisStoreOutboundChannelAdapterParser;
import org.springframework.integration.redis.outbound.RedisPublishingMessageHandler;
import org.springframework.integration.redis.outbound.RedisQueueOutboundChannelAdapter;
import org.springframework.integration.redis.outbound.RedisStoreWritingMessageHandler;
import org.springframework.messaging.MessageHandler;

/**
 * A sink that can be used to insert data into a Redis store.
 *
 * @author Eric Bottard
 */
@EnableModule(Sink.class)
public class RedisSink {

	@Autowired
	private Sink sink;

	@Autowired
	@RedisSinkQualifier
	private RedisConnectionFactory redisConnectionFactory;

	@Autowired
	private RedisSinkModuleOptions moduleOptions;

	@Bean
	public RedisSinkModuleOptions moduleOptions() {
		return new RedisSinkModuleOptions();
	}

	@RedisSinkQualifier
	@Autowired
	private MessageHandler messageHandler;

	@Bean
	@ConditionalOnExpression("'${module.topic}'.substring(0,1)!='$' || '${module.topicExpression}'.substring(0,1)!='$'")
	@RedisSinkQualifier
	public MessageHandler topicExpressionMessageHandler() {
		RedisPublishingMessageHandler redisPublishingMessageHandler = new RedisPublishingMessageHandler(redisConnectionFactory);
		redisPublishingMessageHandler.setTopicExpression(moduleOptions.getTopicExpression());
		return redisPublishingMessageHandler;
	}

	@Bean
	@ConditionalOnExpression("'${module.queue}'.substring(0,1)!='$' || '${module.queueExpression}'.substring(0,1)!='$'")
	@RedisSinkQualifier
	public MessageHandler queueExpressionMessageHandler() {
		RedisQueueOutboundChannelAdapter redisQueueOutboundChannelAdapter = new RedisQueueOutboundChannelAdapter(moduleOptions.getQueueExpression(), redisConnectionFactory);
		return redisQueueOutboundChannelAdapter;
	}

	@Bean
	@ConditionalOnExpression("'${module.key}'.substring(0,1)!='$' || '${module.keyExpression}'.substring(0,1)!='$'")
	@RedisSinkQualifier
	public MessageHandler storeExpressionMessageHandler() {
		RedisStoreWritingMessageHandler redisStoreWritingMessageHandler = new RedisStoreWritingMessageHandler(redisConnectionFactory);
		redisStoreWritingMessageHandler.setKeyExpression(moduleOptions.getKeyExpression());
		return redisStoreWritingMessageHandler;
	}

	@Bean
	public ConsumerEndpointFactoryBean consumerEndpointFactoryBean() {
		ConsumerEndpointFactoryBean consumerEndpointFactoryBean = new ConsumerEndpointFactoryBean();
		consumerEndpointFactoryBean.setInputChannel(sink.input());
		consumerEndpointFactoryBean.setHandler(messageHandler);
		return consumerEndpointFactoryBean;
	}

}
