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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.util.Assert;

/**
 * Represents the data stored in a Counter for multiple field-value pairs. Operations on it are expected to increment or
 * decrement the value associated with a specific field name. The name property is a friendly user assigned name, and
 * should be unique.
 * 
 * @author Mark Pollack
 * @author Ilayaperumal Gopinathan
 */
public final class FieldValueCounter {

	private final String name;

	private final Map<String, Double> fieldValueCounts;

	public FieldValueCounter(String name) {
		Assert.notNull(name);
		this.name = name;
		this.fieldValueCounts = new ConcurrentHashMap<>();
	}

	public FieldValueCounter(String name, Map<String, Double> fieldValueCounts) {
		Assert.notNull(name);
		Assert.notNull(fieldValueCounts);
		this.name = name;
		this.fieldValueCounts = fieldValueCounts;
	}

	public String getName() {
		return name;
	}

	public Map<String, Double> getFieldValueCounts() {
		return this.fieldValueCounts;
	}

	@Override
	public String toString() {
		return "FieldValueCounter [name=" + name + ", fieldValueCounts=" + fieldValueCounts + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof FieldValueCounter)) {
			return false;
		}
		FieldValueCounter other = (FieldValueCounter) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		}
		else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}

}
