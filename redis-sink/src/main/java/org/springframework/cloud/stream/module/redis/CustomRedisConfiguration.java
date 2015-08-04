package org.springframework.cloud.stream.module.redis;

import java.net.UnknownHostException;

import org.apache.commons.pool2.impl.GenericObjectPool;
import redis.clients.jedis.Jedis;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.redis.RedisProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnection;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisOperations;

/**
 * Creates a dedicated RedisConnectionFactory (that is, different from the spring-cloud-stream one).
 *
 * @author Eric Bottard
 */
@Configuration
@ConditionalOnClass({ JedisConnection.class, RedisOperations.class, Jedis.class })
@EnableConfigurationProperties
public class CustomRedisConfiguration extends RedisAutoConfiguration {

	@Bean(name = "redis.sink.RedisProperties")
	@ConfigurationProperties("module")
	public RedisProperties redisProperties() {
		return new RedisProperties();
	}


	@Configuration
	@ConditionalOnMissingClass("org.apache.commons.pool2.impl.GenericObjectPool")
	protected static class CustomRedisConnectionConfiguration extends RedisConnectionConfiguration {

		@Bean
		@RedisSinkQualifier
		public JedisConnectionFactory redisConnectionFactory()
				throws UnknownHostException {
			return super.redisConnectionFactory();
		}
	}

	@Configuration
	@ConditionalOnClass(GenericObjectPool.class)
	protected static class CustomRedisPooledConnectionConfiguration extends
			RedisPooledConnectionConfiguration {

		@Bean
		@RedisSinkQualifier
		public JedisConnectionFactory redisConnectionFactory()
				throws UnknownHostException {
			return super.redisConnectionFactory();
		}


	}
}
