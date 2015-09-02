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
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.cloud.stream.module.common.ScriptVariableGeneratorConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.integration.annotation.Filter;
import org.springframework.integration.groovy.GroovyScriptExecutingMessageProcessor;
import org.springframework.integration.handler.MessageProcessor;
import org.springframework.integration.scripting.ScriptVariableGenerator;
import org.springframework.scripting.support.ResourceScriptSource;

/**
 * A Processor module that retains or discards messages according to a predicate,
 * expressed as a Groovy script.
 *
 * @author Eric Bottard
 * @author Mark Fisher
 * @author Marius Bogoevici
 */
@EnableBinding(Processor.class)
@Import(ScriptVariableGeneratorConfiguration.class)
@EnableConfigurationProperties(GroovyFilterProcessorProperties.class)
public class GroovyFilterProcessor {

	@Autowired
	private GroovyFilterProcessorProperties properties;

	@Autowired
	private ScriptVariableGenerator scriptVariableGenerator;

	@Bean
	@Filter(inputChannel = Processor.INPUT, outputChannel = Processor.OUTPUT)
	public MessageProcessor<?> filter() {
		return new GroovyScriptExecutingMessageProcessor(
				new ResourceScriptSource(properties.getScript()), scriptVariableGenerator);
	}

}
