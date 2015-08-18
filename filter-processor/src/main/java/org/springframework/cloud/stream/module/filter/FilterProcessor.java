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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableModule;
import org.springframework.cloud.stream.annotation.Processor;
import org.springframework.cloud.stream.annotation.Sink;
import org.springframework.cloud.stream.module.common.ScriptModuleVariableConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.annotation.Transformer;
import org.springframework.integration.config.FilterFactoryBean;
import org.springframework.integration.config.TransformerFactoryBean;
import org.springframework.integration.groovy.GroovyScriptExecutingMessageProcessor;
import org.springframework.integration.scripting.ScriptVariableGenerator;
import org.springframework.messaging.MessageHandler;
import org.springframework.scripting.support.ResourceScriptSource;

/**
 * A Processor module that allows one to discard or retain messages according to a predicate.
 *
 * @author Eric Bottard
 */
@EnableModule(Processor.class)
@Import(ScriptModuleVariableConfiguration.class)
@EnableConfigurationProperties(FilterProcessorProperties.class)
public class FilterProcessor {

	@Autowired
	private Processor channels;

	@Autowired
	private FilterProcessorProperties properties;

	@Autowired(required = false)
	private ScriptVariableGenerator scriptVariableGenerator;

	@Bean
	@Transformer(inputChannel = Processor.INPUT)
	public MessageHandler transformer(BeanFactory beanFactory) throws Exception {

		FilterFactoryBean factoryBean = new FilterFactoryBean();
		factoryBean.setBeanFactory(beanFactory);
		factoryBean.setOutputChannel(channels.output());
		if (properties.getScript() != null) {
			GroovyScriptExecutingMessageProcessor
					processor = new GroovyScriptExecutingMessageProcessor(
					new ResourceScriptSource(properties.getScript()), scriptVariableGenerator);
			factoryBean.setTargetObject(processor);
		}
		else {
			factoryBean.setExpression(properties.getExpression());
		}

		return factoryBean.getObject();
	}

}
