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

package org.springframework.cloud.stream.module.http;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;

/**
 * Holds configuration properties for the Http Source.
 *
 * @author Eric Bottard
 */
@ConfigurationProperties
public class HttpSourceProperties {

	private static final SpelExpressionParser PARSER = new SpelExpressionParser();

	/**
	 * A SpEL expression to evaluate in order to generate the Message payload.
	 */
	private Expression payloadExpression;

	/**
	 * A Map of SpEL expressions to evaluate in order to generate the Message headers.
	 */
	private Map<String, Expression> headerExpressions;

	public Map<String, Expression> getHeaderExpressions() {
		return headerExpressions;
	}

	public void setHeaderExpressions(Map<String, String> headerExpressions) {
		this.headerExpressions = new HashMap<>();
		for (Map.Entry<String, String> kv : headerExpressions.entrySet()) {
			this.headerExpressions.put(kv.getKey(), PARSER.parseExpression(kv.getValue()));
		}
	}

	public Expression getPayloadExpression() {
		return payloadExpression;
	}

	public void setPayloadExpression(String payloadExpression) {
		this.payloadExpression = PARSER.parseExpression(payloadExpression);
	}
}
