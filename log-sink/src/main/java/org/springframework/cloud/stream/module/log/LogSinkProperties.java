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

import static org.springframework.integration.handler.LoggingHandler.Level.*;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.integration.handler.LoggingHandler;

/**
 * Configuration properties for the Log Sink module.
 *
 * @author Gary Russell
 * @author Eric Bottard
 */
@ConfigurationProperties
public class LogSinkProperties {

	/**
	 * The name of the logger to use.
	 */
	@Value("${spring.application.name:log.sink}")
	private String name;

	/**
	 * A SpEL expression (against the incoming message) to evaluate as the logged message.
	 */
	private String expression = "payload";

	/**
	 * The level at which to log messages.
	 */
	private LoggingHandler.Level level = INFO;

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

	@NotNull
	public LoggingHandler.Level getLevel() {
		return level;
	}

	public void setLevel(LoggingHandler.Level level) {
		this.level = level;
	}

}
