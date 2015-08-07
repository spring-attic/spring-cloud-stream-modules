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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableModule;
import org.springframework.cloud.stream.annotation.Processor;
import org.springframework.cloud.stream.config.SpelExpressionConverterConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;

/**
 * A Processor module that allows one to discard or retain messages according to a predicate.
 *
 * @author Eric Bottard
 */
@EnableModule(Processor.class)
@Import(SpelExpressionConverterConfiguration.class)
@EnableConfigurationProperties(FilterProcessorProperties.class)
public class FilterProcessor {

	@Autowired
	private FilterProcessorProperties properties;

	@ServiceActivator(inputChannel = Processor.INPUT, outputChannel = Processor.OUTPUT)
	public <T> Message<T> filter(Message<T> in) {
		if (properties.getExpression().getValue(in, Boolean.class)) {
			return in;
		} else {
			return null;
		}
	}

}
