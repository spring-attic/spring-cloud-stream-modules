package org.springframework.cloud.stream.module.redis.sink;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.redis.RedisProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnection;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.integration.redis.outbound.RedisPublishingMessageHandler;
import org.springframework.integration.redis.outbound.RedisQueueOutboundChannelAdapter;
import org.springframework.integration.redis.outbound.RedisStoreWritingMessageHandler;
import org.springframework.messaging.MessageHandler;
import redis.clients.jedis.Jedis;

import java.net.UnknownHostException;

/**
 * Creates a dedicated RedisConnectionFactory different from the one spring-boot autoconfigure
 * section that the bindings use in spring-cloud-stream.
 *
 * The configuration prefix is "spring.cloud.stream.module.redis" and contains the standard
 * properties to configure a redis connection, host, port, etc as well as the additional properties for
 * the sink, queue, key, etc.
 *
 * @author Eric Bottard
 * @author Mark Pollack
 */
@Configuration
@ConditionalOnClass({ JedisConnection.class, RedisOperations.class, Jedis.class })
@EnableConfigurationProperties(RedisSinkProperties.class)
public class RedisSinkAutoConfiguration {

	@Autowired
	private RedisConnectionFactory redisConnectionFactory;

	@Autowired
	private RedisSinkProperties moduleOptions;

	@Bean
	@Qualifier
	public MessageHandler redisSinkMessageHandler() {
		if (moduleOptions.isKey()) {
			RedisStoreWritingMessageHandler redisStoreWritingMessageHandler =
					new RedisStoreWritingMessageHandler(redisConnectionFactory);
			redisStoreWritingMessageHandler.setKeyExpression(moduleOptions.getKeyExpression());
			return redisStoreWritingMessageHandler;
		} else if (moduleOptions.isQueue()) {
			return new RedisQueueOutboundChannelAdapter(moduleOptions.getQueueExpression(), redisConnectionFactory);
		} else { // must be topic
			RedisPublishingMessageHandler redisPublishingMessageHandler =
					new RedisPublishingMessageHandler(redisConnectionFactory);
			redisPublishingMessageHandler.setTopicExpression(moduleOptions.getTopicExpression());
			return redisPublishingMessageHandler;
		}
	}
}
