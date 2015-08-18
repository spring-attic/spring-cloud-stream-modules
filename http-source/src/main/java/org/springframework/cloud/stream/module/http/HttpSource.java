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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.annotation.EnableModule;
import org.springframework.cloud.stream.annotation.Source;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.integration.http.inbound.HttpRequestHandlingController;
import org.springframework.integration.http.inbound.RequestMapping;
import org.springframework.web.servlet.mvc.Controller;

/**
 * A source module that listens for http connections as an endpoint and emits the
 * http body (or request params) as a message payload.
 *
 * @author Eric Bottard
 */
@EnableModule(Source.class)
@EnableConfigurationProperties({HttpSourceProperties.class, RequestMapping.class})
public class HttpSource {

	@Autowired
	private HttpSourceProperties properties;

	@Autowired
	private RequestMapping requestMapping;

	@Autowired
	private Source channels;

	@Bean
	public Controller inboundController() {
		HttpRequestHandlingController controller = new HttpRequestHandlingController(false);
		controller.setRequestChannel(channels.output());
		controller.setRequestMapping(requestMapping);
		controller.setPayloadExpression(properties.getPayloadExpression());
		controller.setHeaderExpressions(properties.getHeaderExpressions());
		String viewExpression = "new " + DefaultResultView.class.getName() + "()";
		controller.setViewExpression(new SpelExpressionParser().parseExpression(viewExpression));
		return controller;
	}
}
