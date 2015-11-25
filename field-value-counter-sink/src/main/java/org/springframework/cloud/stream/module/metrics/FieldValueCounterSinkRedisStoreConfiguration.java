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

package org.springframework.cloud.stream.module.metrics;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.retry.RetryOperations;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

/**
 * Configuration class for Redis based field value counter.
 *
 * @author Ilayaperumal Gopinathan
 */
@Configuration
@ConditionalOnProperty(value="store", havingValue = "redis")
public class FieldValueCounterSinkRedisStoreConfiguration {

	@Autowired
	RedisConnectionFactory redisConnectionFactory;

	@Bean
	public FieldValueCounterRepository redisMetricRepository() {
		return new RedisFieldValueCounterRepository(redisConnectionFactory, retryOperations());
	}

	@Bean
	public RetryOperations retryOperations() {
		RetryTemplate retryTemplate = new RetryTemplate();
		retryTemplate.setRetryPolicy(new SimpleRetryPolicy(3, Collections.<Class<? extends Throwable>, Boolean>singletonMap(RedisConnectionFailureException.class, true)));
		ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
		backOffPolicy.setInitialInterval(1000L);
		backOffPolicy.setMaxInterval(1000L);
		backOffPolicy.setMultiplier(2);
		retryTemplate.setBackOffPolicy(backOffPolicy);
		return retryTemplate;
	}

}
