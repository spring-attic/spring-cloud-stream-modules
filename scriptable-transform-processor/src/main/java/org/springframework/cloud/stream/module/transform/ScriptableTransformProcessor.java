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
package org.springframework.cloud.stream.module.transform;

import java.util.regex.Matcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.cloud.stream.module.common.ScriptVariableGeneratorConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.integration.annotation.Transformer;
import org.springframework.integration.handler.MessageProcessor;
import org.springframework.integration.scripting.ScriptExecutor;
import org.springframework.integration.scripting.ScriptVariableGenerator;
import org.springframework.integration.scripting.jsr223.ScriptExecutingMessageProcessor;
import org.springframework.integration.scripting.jsr223.ScriptExecutorFactory;
import org.springframework.scripting.ScriptSource;
import org.springframework.scripting.support.StaticScriptSource;

/**
 * A Processor module that transforms messages using a supplied script. The script
 * code is passed in directly via property. For more information on Spring script 
 * processing, see
 * <a href=
 * "https://spring.io/blog/2011/12/08/spring-integration-scripting-support-part-1">
 * this blog article</a>.
 *
 * @author Andy Clement
 */
@EnableBinding(Processor.class)
@Import(ScriptVariableGeneratorConfiguration.class)
@EnableConfigurationProperties(ScriptableTransformProcessorProperties.class)
public class ScriptableTransformProcessor {

	private static final String NEWLINE_ESCAPE = Matcher.quoteReplacement("\\n");

	private static final String DOUBLE_DOUBLE_QUOTE = Matcher.quoteReplacement("\"\"");

	private static Logger logger = LoggerFactory.getLogger(ScriptableTransformProcessor.class);

	@Autowired
	private ScriptVariableGenerator scriptVariableGenerator;

	@Autowired
	private ScriptableTransformProcessorProperties properties;

	@Bean
	@Transformer(inputChannel = Processor.INPUT, outputChannel = Processor.OUTPUT)
	public MessageProcessor<?> transformer() {
		String language = properties.getLanguage();
		String script = properties.getScript();
		logger.info("Input script is '{}'", script);
		ScriptSource scriptSource = new StaticScriptSource(decodeScript(script));
		ScriptExecutor scriptExecutor = ScriptExecutorFactory.getScriptExecutor(language);
		if (scriptExecutor == null) {
			logger.error("Unable to obtain script executor for language: " + language);
			return null; // do not go on to return something that can't work
		}
		return new ScriptExecutingMessageProcessor(scriptSource, scriptVariableGenerator, scriptExecutor);
	}

	private static String decodeScript(String script) {
		String toProcess = script;
		// If it has both a leading and trailing double quote, remove them
		if (toProcess.startsWith("\"") && toProcess.endsWith("\"")) {
			toProcess = script.substring(1, script.length() - 1);
		}
		return toProcess.replaceAll(NEWLINE_ESCAPE, "\n").replaceAll(DOUBLE_DOUBLE_QUOTE, "\"");
	}

}
