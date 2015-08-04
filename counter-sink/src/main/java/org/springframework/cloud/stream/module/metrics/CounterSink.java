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

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableModule;
import org.springframework.cloud.stream.annotation.Sink;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.context.IntegrationContextUtils;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.util.Assert;

/**
 * A simple module that counts messages received, using Spring Boot metrics abstraction.
 *
 * @author Eric Bottard
 */
@EnableModule(Sink.class)
@EnableConfigurationProperties(CounterSinkOptions.class)
public class CounterSink {

	private static Logger logger = LoggerFactory.getLogger(CounterSink.class);

	private CounterService counterService;

	private CounterSinkOptions options;

	// Default value useful for tests
	private EvaluationContext evaluationContext = new StandardEvaluationContext();

	private boolean evaluationContextSet;

	private BeanFactory beanFactory;

	@Autowired
	public void setOptions(CounterSinkOptions options) {
		this.options = options;
	}

	@Autowired
	public void setCounterService(CounterService counterService) {
		this.counterService = counterService;
	}

	@ServiceActivator(inputChannel=Sink.INPUT)
	public void counterSink(Message<?> message) {
		String name = computeMetricName(message);
		logger.debug("Received: {}, about to increment counter named '{}'", message, name);
		counterService.increment(name);
	}

	protected String computeMetricName(Message<?> message) {
		if (options.getName() != null) {
			return options.getName();
		} else {
			return options.getNameExpression().getValue(evaluationContext, message, CharSequence.class).toString();
		}
	}

	public void setEvaluationContext(EvaluationContext evaluationContext) {
		Assert.notNull(evaluationContext, "'evaluationContext' cannot be null");
		this.evaluationContext = evaluationContext;
		this.evaluationContextSet = true;
	}

	@Autowired
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	@PostConstruct
	public void afterPropertiesSet() throws Exception {
		if (!this.evaluationContextSet) {
			this.evaluationContext = IntegrationContextUtils.getEvaluationContext(this.beanFactory);
		}
	}

}
