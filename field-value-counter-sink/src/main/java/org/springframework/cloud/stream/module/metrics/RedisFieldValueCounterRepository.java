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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.springframework.cloud.stream.module.retry.StringRedisRetryTemplate;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.retry.RetryOperations;
import org.springframework.util.Assert;

public class RedisFieldValueCounterRepository implements FieldValueCounterRepository {

	private final String metricPrefix;

	private final StringRedisRetryTemplate redisTemplate;

	private static final String MARKER = "_marker_";

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

	/*
	 * Note: Handler implementations typically use increment() variants to save state. The save() contract
	 * is to store the counter as a whole. Simplest approach is erase/rewrite.
	 */
	public <S extends FieldValueCounter> S save(S fieldValueCounter) {
		reset(fieldValueCounter.getName(), MARKER);
		increment(fieldValueCounter.getName(), MARKER, 0);
		for (Map.Entry<String, Double> entry : fieldValueCounter.getFieldValueCounts().entrySet()) {
			increment(fieldValueCounter.getName(), entry.getKey(), entry.getValue());
		}
		return fieldValueCounter;
	}

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
	public void increment(String counterName, String fieldName, double score) {
		redisTemplate.boundZSetOps(getMetricKey(counterName)).incrementScore(fieldName, score);
	}

	@Override
	public void decrement(String counterName, String fieldName, double score) {
		redisTemplate.boundZSetOps(getMetricKey(counterName)).incrementScore(fieldName, -score);
	}

	@Override
	public void reset(String counterName, String fieldName) {
		redisTemplate.boundZSetOps(getMetricKey(counterName)).remove(fieldName);
	}


	/**
	 * Provides the key for a named metric. By default this appends the name to the metricPrefix value.
	 * 
	 * @param metricName the name of the metric
	 * @return the redis key under which the metric is stored
	 */
	protected String getMetricKey(String metricName) {
		return metricPrefix + metricName;
	}

	protected Map<String, Double> getZSetData(String counterKey) {
		Set<ZSetOperations.TypedTuple<String>> rangeWithScore = this.redisTemplate
				.boundZSetOps(counterKey).rangeWithScores(0, -1);
		Map<String, Double> values = new HashMap<String, Double>(
				rangeWithScore.size());
		for (Iterator<ZSetOperations.TypedTuple<String>> iterator = rangeWithScore.iterator(); iterator
				.hasNext();) {
			ZSetOperations.TypedTuple<String> typedTuple = iterator.next();
			if (!typedTuple.getValue().equals(MARKER)) {
				values.put(typedTuple.getValue(), typedTuple.getScore());
			}
		}
		return values;
	}

}
