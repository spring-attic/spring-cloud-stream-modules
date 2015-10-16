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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.handler.LoggingHandler;

/**
 * @author Dave Syer
 * @author Marius Bogoevici
 * @author Gary Russell
 */
@EnableBinding(Sink.class)
@EnableConfigurationProperties(LogSinkProperties.class)
public class LogSink {

	@Autowired
	private LogSinkProperties properties;

	@Bean
	@ServiceActivator(inputChannel=Sink.INPUT)
	public LoggingHandler logSinkHandler() {
		LoggingHandler loggingHandler = new LoggingHandler(this.properties.getLevel().name());
		loggingHandler.setExpression(this.properties.getExpression());
		loggingHandler.setLoggerName(this.properties.getName());
		return loggingHandler;
	}

}
