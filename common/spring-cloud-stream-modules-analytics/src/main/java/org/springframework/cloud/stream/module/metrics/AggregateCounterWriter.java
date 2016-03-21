/*
 * Copyright 2016 the original author or authors.
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

import org.joda.time.DateTime;

/**
 * Contains operations to modify/reset AggregateCounter instances.
 *
 * @author Ilayaperumal Gopinathan
 */
public interface AggregateCounterWriter {

	/**
	 * Increments the named counter by a specific amount for the given instant.
	 */
	long increment(String name, long amount, DateTime dateTime);

	/**
	 * Reset the given AggregateCounter.
	 *
	 * @param name the AggregateCounter name
	 */
	void reset(String name);
}
