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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.ExportMetricWriter;
import org.springframework.boot.actuate.metrics.repository.redis.RedisMetricRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * Used to configure the connection and offloading of counters to redis in case it is used as
 * a store.
 *
 * @author Eric Bottard
 * @see CounterSinkProperties#getStore()
 */
@Configuration
@ConditionalOnProperty(value="store", havingValue = "redis")
public class CounterSinkRedisStoreConfiguration {

	@Autowired
	RedisConnectionFactory redisConnectionFactory;

	@Bean
	@ExportMetricWriter
	public RedisMetricRepository redisMetricRepository() {
		return new RedisMetricRepository(redisConnectionFactory);
	}
}
