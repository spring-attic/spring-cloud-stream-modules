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

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import javax.validation.constraints.AssertTrue;

/**
 * Holds configuration options for the Counter Sink.
 *
 * @author Eric Bottard
 */
@ConfigurationProperties
public class CounterSinkProperties {

	private final SpelExpressionParser spelExpressionParser = new SpelExpressionParser();

	/**
	 * The name of the counter to increment.
	 */
	private String name;

	/**
	 * A SpEL expression (against the incoming Message) to derive the name of the counter to increment.
	 */
	private String nameExpression;

	/**
	 * The name of a backend used to store the counter.
	 */
	// Stored as a String to allow forward extension of the module
	private String backend = "memory";

	public String getBackend() {
		return backend;
	}

	public void setBackend(String backend) {
		this.backend = backend;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Expression getNameExpression() {
		return spelExpressionParser.parseExpression(nameExpression);
	}

	public void setNameExpression(String nameExpression) {
		this.nameExpression = nameExpression;
	}

	@AssertTrue(message = "exactly one of 'name' and 'nameExpression' must be set")
	public boolean isExclusiveOptions() {
		return name != null ^ nameExpression != null;
	}

}
