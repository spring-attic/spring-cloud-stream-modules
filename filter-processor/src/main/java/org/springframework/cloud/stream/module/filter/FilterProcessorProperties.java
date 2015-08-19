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

package org.springframework.cloud.stream.module.filter;

import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

/**
 * Holds configuration properties for the Filter Processor module.
 *
 * @author Eric Bottard
 */
@ConfigurationProperties
public class FilterProcessorProperties {

	private static final Expression DEFAULT_EXPRESSION = new SpelExpressionParser().parseExpression("true");

	/**
	 * A SpEL expression to be evaluated against each message, to decide whether or not to discard it.
	 */
	private Expression expression = DEFAULT_EXPRESSION;

	public void setExpression(Expression expression) {
		this.expression = expression;
	}

	@NotNull
	public Expression getExpression() {
		return expression;
	}

}
