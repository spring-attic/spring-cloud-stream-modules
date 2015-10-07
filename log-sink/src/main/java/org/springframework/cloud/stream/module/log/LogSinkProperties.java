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
package org.springframework.cloud.stream.module.log;

import org.hibernate.validator.constraints.NotBlank;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Gary Russell
 *
 */
@ConfigurationProperties
public class LogSinkProperties {

	private String name = "log.sink"; // TODO: set from environment 'group' if available

	private String expression = "payload";

	private String level = "INFO";

	@NotBlank
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@NotBlank
	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	@NotBlank
	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level.toUpperCase();
	}

}
