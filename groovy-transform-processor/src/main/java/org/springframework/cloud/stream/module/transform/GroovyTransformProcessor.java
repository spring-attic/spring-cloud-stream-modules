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

package org.springframework.cloud.stream.module.transform;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableModule;
import org.springframework.cloud.stream.annotation.Processor;
import org.springframework.cloud.stream.module.common.ScriptModuleVariableConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.integration.annotation.Transformer;
import org.springframework.integration.config.TransformerFactoryBean;
import org.springframework.integration.groovy.GroovyScriptExecutingMessageProcessor;
import org.springframework.integration.scripting.ScriptVariableGenerator;
import org.springframework.messaging.MessageHandler;
import org.springframework.scripting.support.ResourceScriptSource;

/**
 * A Processor module that allows transformation of messages using a Groovy script.
 *
 * @author Eric Bottard
 */
@EnableModule(Processor.class)
@Import(ScriptModuleVariableConfiguration.class)
@EnableConfigurationProperties(GroovyTransformProcessorProperties.class)
public class GroovyTransformProcessor {

	@Autowired
	private ScriptVariableGenerator scriptVariableGenerator;

	@Autowired
	private Processor channels;

	@Autowired
	private GroovyTransformProcessorProperties properties;

	@Bean
	@Transformer(inputChannel = Processor.INPUT)
	public MessageHandler transformer(BeanFactory beanFactory) throws Exception {

		TransformerFactoryBean factoryBean = new TransformerFactoryBean();
		factoryBean.setBeanFactory(beanFactory);
		factoryBean.setOutputChannel(channels.output());
		GroovyScriptExecutingMessageProcessor
				processor = new GroovyScriptExecutingMessageProcessor(
				new ResourceScriptSource(properties.getScript()), scriptVariableGenerator);
		factoryBean.setTargetObject(processor);
		return factoryBean.getObject();
	}

}
