/*
 * Copyright 2015 the original author or authors.
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

package org.springframework.cloud.stream.module;

import java.util.concurrent.TimeUnit;

import javax.validation.constraints.Min;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author David Turanski
 */
@ConfigurationProperties
public class PeriodicTriggerProperties {

	/**
	 * Fixed delay for periodic triggers. Default is 1 TimeUnit. 
	 */
	private int fixedDelay = 1;

	/**
	 * Initial delay for periodic triggers. Default is 0. 
	 */
	private int initialDelay = 0;

	/**
	 * The TimeUnit to apply to delay values. Default is TimeUnit.SECONDS
	 */
	private TimeUnit timeUnit = TimeUnit.SECONDS;

	@Min(0)
	public int getInitialDelay() {
		return initialDelay;
	}

	public void setInitialDelay(int initialDelay) {
		this.initialDelay = initialDelay;
	}

	public TimeUnit getTimeUnit() {
		return timeUnit;
	}

	public void setTimeUnit(TimeUnit timeUnit) {
		this.timeUnit = timeUnit;
	}

	public int getFixedDelay() { return fixedDelay; }

	public void setFixedDelay(int fixedDelay) { this.fixedDelay = fixedDelay; }
}
