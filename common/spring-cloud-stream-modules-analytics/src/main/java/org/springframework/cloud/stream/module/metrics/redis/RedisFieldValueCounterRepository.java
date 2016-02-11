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
package org.springframework.cloud.stream.module.metrics.redis;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.springframework.cloud.stream.module.metrics.FieldValueCounter;
import org.springframework.cloud.stream.module.metrics.FieldValueCounterRepository;
import org.springframework.cloud.stream.module.retry.StringRedisRetryTemplate;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.retry.RetryOperations;
import org.springframework.util.Assert;

public class RedisFieldValueCounterRepository implements FieldValueCounterRepository {

	private final String metricPrefix;

	private final StringRedisRetryTemplate redisTemplate;

	public RedisFieldValueCounterRepository(RedisConnectionFactory connectionFactory, RetryOperations retryOperations) {
		this(connectionFactory, "fieldvaluecounters.", retryOperations);
	}

	public RedisFieldValueCounterRepository(RedisConnectionFactory connectionFactory, String metricPrefix,
											RetryOperations retryOperations) {
		Assert.notNull(connectionFactory);
		Assert.hasText(metricPrefix, "metric prefix cannot be empty");
		this.metricPrefix = metricPrefix;
		redisTemplate = new StringRedisRetryTemplate(connectionFactory, retryOperations);
		// avoids proxy
		redisTemplate.setExposeConnection(true);
		redisTemplate.afterPropertiesSet();
	}

	@Override
	public FieldValueCounter findOne(String name) {
		Assert.notNull(name, "The name of the FieldValueCounter must not be null");
		String metricKey = getMetricKey(name);
		if (redisTemplate.hasKey(metricKey)) {
			Map<String, Double> values = getZSetData(metricKey);
			FieldValueCounter c = new FieldValueCounter(name, values);
			return c;
		}
		else {
			return null;
		}
	}

	@Override
	public Collection<String> list() {
		Set<String> keys = redisTemplate.keys(getMetricKey("*"));
		Set<String> names = new HashSet<>(keys.size());
		for (String key : keys) {
			names.add(getCounterName(key));
		}
		return names;
	}

	@Override
	public void increment(String counterName, String fieldName, double score) {
		redisTemplate.boundZSetOps(getMetricKey(counterName)).incrementScore(fieldName, score);
	}

	@Override
	public void decrement(String counterName, String fieldName, double score) {
		redisTemplate.boundZSetOps(getMetricKey(counterName)).incrementScore(fieldName, -score);
	}

	@Override
	public void reset(String counterName) {
		redisTemplate.delete(getMetricKey(counterName));
	}


	/**
	 * Provides the key for a named metric. By default this prepends the name to the metricPrefix value.
	 * 
	 * @param metricName the name of the metric
	 * @return the redis key under which the metric is stored
	 */
	protected String getMetricKey(String metricName) {
		return metricPrefix + metricName;
	}

	/**
	 * Provides the name of a counter stored under a given key. This operation is the reverse of {@link #getMetricKey(String)}.
	 */
	private String getCounterName(String redisKey) {
		return redisKey.substring(metricPrefix.length());
	}

	protected Map<String, Double> getZSetData(String counterKey) {
		Set<ZSetOperations.TypedTuple<String>> rangeWithScore = this.redisTemplate
				.boundZSetOps(counterKey).rangeWithScores(0, -1);
		Map<String, Double> values = new HashMap<String, Double>(
				rangeWithScore.size());
		for (Iterator<ZSetOperations.TypedTuple<String>> iterator = rangeWithScore.iterator(); iterator
				.hasNext();) {
			ZSetOperations.TypedTuple<String> typedTuple = iterator.next();
			values.put(typedTuple.getValue(), typedTuple.getScore());
		}
		return values;
	}

}
