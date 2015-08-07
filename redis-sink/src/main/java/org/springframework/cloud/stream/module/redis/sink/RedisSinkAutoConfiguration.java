package org.springframework.cloud.stream.module.redis.sink;

import org.apache.commons.pool2.impl.GenericObjectPool;
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
import org.springframework.data.redis.connection.jedis.JedisConnection;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisOperations;
import redis.clients.jedis.Jedis;

import java.net.UnknownHostException;

/**
 * Creates a dedicated RedisConnectionFactory different from the one spring-boot autoconfigures
 * The configuration prefix is "spring.cloud.stream.module.redis"
 *
 * @author Eric Bottard
 */
@Configuration
@ConditionalOnClass({ JedisConnection.class, RedisOperations.class, Jedis.class })
@EnableConfigurationProperties
public class RedisSinkAutoConfiguration extends RedisAutoConfiguration {

	@Bean(name = "org.springframework.cloud.stream.module.redis.sink.RedisProperties")
	@ConfigurationProperties("spring.cloud.stream.module.redis.sink")
	@ConditionalOnMissingBean
	public RedisProperties redisProperties() {
		return new RedisProperties();
	}

	@Bean(name = "org.springframework.cloud.stream.module.redis.sink.RedisSinkProperties")
	@ConfigurationProperties("spring.cloud.stream.module.redis.sink")
	@ConditionalOnMissingBean
	public RedisSinkProperties redisSinkProperties() {
		return new RedisSinkProperties();
	}


	@Configuration
	@ConditionalOnMissingClass("org.apache.commons.pool2.impl.GenericObjectPool")
	protected static class CustomRedisConnectionConfiguration extends RedisConnectionConfiguration {

		@Bean
		@Qualifier("redisSink")
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
		@Qualifier("redisSink")
		public JedisConnectionFactory redisConnectionFactory()
				throws UnknownHostException {
			return super.redisConnectionFactory();
		}


	}
}
