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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.util.Assert;

/**
 * Memory backed implementation of MetricRepository that uses a ConcurrentMap
 *
 * @author Luke Taylor
 *
 */
public abstract class InMemoryMetricRepository<M extends Metric> implements MetricRepository<M, String> {

	private final ConcurrentMap<String, M> map = new ConcurrentHashMap<String, M>();

	@Override
	public <S extends M> S save(S metric) {
		map.put(metric.getName(), metric);
		return metric;
	}

	@Override
	public M findOne(String name) {
		Assert.notNull(name, "The name of the metric must not be null");
		return map.get(name);
	}

	protected M getOrCreate(String name) {
		synchronized (map) {
			M result = findOne(name);
			if (result == null) {
				result = create(name);
				result = save(result);
			}
			return result;
		}
	}

	protected abstract M create(String name);
}
