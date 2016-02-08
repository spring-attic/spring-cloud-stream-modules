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
package org.springframework.cloud.stream.module.router.sink;

import java.util.Properties;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.expression.Expression;
import org.springframework.integration.dsl.support.Function;
import org.springframework.integration.dsl.support.FunctionExpression;
import org.springframework.messaging.Message;

/**
 * Properties for the Router Sink; the router can use an expression
 * or groovy script to return either a channel name, or a key to
 * the channel mappings map.
 *
 * @author Gary Russell
 *
 */
@ConfigurationProperties
public class RouterSinkProperties {

	public static final Expression DEFAULT_EXPRESSION = new FunctionExpression<>(new Function<Message<?>, Object>() {

		@Override
		public Object apply(Message<?> message) {
			return message.getHeaders().get("routeTo");
		}

	});

	/**
	 * The expression to be applied to the message to determine the channel(s)
	 * to route to.
	 */
	private Expression expression = DEFAULT_EXPRESSION;

	/**
	 * The location of a groovy script that returns channels or channel mapping
	 * resolution keys.
	 */
	private Resource script;

	/**
	 * How often to check for script changes in ms (if present); < 0 means don't refresh.
	 */
	private int refreshDelay = 60000;

	/**
	 * Where to send unroutable messages.
	 */
	private String defaultOutputChannel = "nullChannel";

	/**
	 * Whether or not channel resolution is required.
	 */
	private boolean resolutionRequired = false;


	/**
	 * Destination mappings as a new line delimited string of name-value pairs, e.g. 'foo=bar\n baz=car'.
	 */
	private Properties destinationMappings;

	public Expression getExpression() {
		return this.expression;
	}

	public void setExpression(Expression expression) {
		this.expression = expression;
	}

	public Resource getScript() {
		return script;
	}

	public void setScript(Resource script) {
		this.script = script;
	}

	@NotNull
	public String getDefaultOutputChannel() {
		return this.defaultOutputChannel;
	}

	public int getRefreshDelay() {
		return refreshDelay;
	}

	public void setRefreshDelay(int refreshDelay) {
		this.refreshDelay = refreshDelay;
	}

	public void setDefaultOutputChannel(String defaultOutputChannel) {
		this.defaultOutputChannel = defaultOutputChannel;
	}

	public boolean isResolutionRequired() {
		return this.resolutionRequired;
	}

	public void setResolutionRequired(boolean resolutionRequired) {
		this.resolutionRequired = resolutionRequired;
	}

	public Properties getDestinationMappings() {
		return destinationMappings;
	}

	public void setDestinationMappings(Properties destinationMappings) {
		this.destinationMappings = destinationMappings;
	}

	@AssertTrue(message = "'expression' and 'script' are mutually exclusive")
	public boolean isExpressionOrScriptValid() {
		return this.script == null || this.expression == DEFAULT_EXPRESSION;
	}

}
