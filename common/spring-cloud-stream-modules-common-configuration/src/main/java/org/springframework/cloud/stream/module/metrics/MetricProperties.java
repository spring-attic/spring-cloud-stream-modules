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

import javax.validation.constraints.AssertTrue;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.expression.common.LiteralExpression;

/**
 * Common configuration properties for the Spring Cloud Stream metric modules.
 *
 * @author Eric Bottard
 * @author Ilayaperumal Gopinathan
 */
@ConfigurationProperties
public class MetricProperties {

	/**
	 * The name of the counter to increment.
	 */
	@Value("${spring.application.name:counts}")
	private String name;

	/**
	 * A SpEL expression (against the incoming Message) to derive the name of the counter to increment.
	 */
	private LiteralExpression nameExpression;

	/**
	 * The name of a store used to store the counter.
	 */
	// Stored as a String to allow forward extension of the module
	private String store = "memory";

	public String getStore() {
		return store;
	}

	public void setStore(String store) {
		this.store = store;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public LiteralExpression getNameExpression() {
		return nameExpression;
	}

	public LiteralExpression getComputedNameExpression() {
		return (nameExpression != null ? nameExpression : new LiteralExpression(name));
}

	public void setNameExpression(LiteralExpression nameExpression) {
		this.nameExpression = nameExpression;
	}

	@AssertTrue(message = "exactly one of 'name' and 'nameExpression' must be set")
	public boolean isExclusiveOptions() {
		return name != null ^ nameExpression != null;
	}
}
