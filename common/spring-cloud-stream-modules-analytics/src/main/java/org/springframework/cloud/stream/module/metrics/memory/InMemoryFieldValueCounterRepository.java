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
package org.springframework.cloud.stream.module.metrics.memory;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.cloud.stream.module.metrics.FieldValueCounter;
import org.springframework.cloud.stream.module.metrics.FieldValueCounterRepository;
import org.springframework.util.Assert;

/**
 * Memory backed implementation of FieldValueCounterRepository that uses a ConcurrentMap
 *
 * @author Mark Pollack
 * @author Ilayaperumal Gopinathan
 *
 */
public class InMemoryFieldValueCounterRepository implements FieldValueCounterRepository {

	private final ConcurrentMap<String, FieldValueCounter> map = new ConcurrentHashMap<>();

	@Override
	public void increment(String name, String fieldName, double score) {
		modifyFieldValue(name, fieldName, score);
	}

	@Override
	public void decrement(String name, String fieldName, double score) {
		modifyFieldValue(name, fieldName, -score);
	}

	@Override
	public void reset(String name) {
		map.remove(name);
	}

	private void modifyFieldValue(String name, String fieldName, double delta) {
		FieldValueCounter counter = getOrCreate(name);
		Map<String, Double> data = counter.getFieldValueCounts();
		double count = data.containsKey(fieldName) ? data.get(fieldName) : 0;
		data.put(fieldName, count + delta);
		save(counter);
	}

	private FieldValueCounter getOrCreate(String name) {
			FieldValueCounter result = findOne(name);
			if (result == null) {
				synchronized (map) {
					result = create(name);
				}
				result = save(result);
			}
			return result;
	}

	private FieldValueCounter save(FieldValueCounter fieldValueCounter) {
		map.put(fieldValueCounter.getName(), fieldValueCounter);
		return fieldValueCounter;
	}

	@Override
	public FieldValueCounter findOne(String name) {
		Assert.notNull(name, "The name of the metric must not be null");
		return map.get(name);
	}

	@Override
	public Collection<String> list() {
		return map.keySet();
	}

	private FieldValueCounter create(String name) {
		return new FieldValueCounter(name);
	}

}
