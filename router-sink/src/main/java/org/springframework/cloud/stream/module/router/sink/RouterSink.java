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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.Bindings;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.binding.BinderAwareChannelResolver;
import org.springframework.cloud.stream.config.SpelExpressionConverterConfiguration;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.cloud.stream.script.ScriptModulePropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.groovy.GroovyScriptExecutingMessageProcessor;
import org.springframework.integration.router.AbstractMappingMessageRouter;
import org.springframework.integration.router.ExpressionEvaluatingRouter;
import org.springframework.integration.router.MethodInvokingRouter;
import org.springframework.integration.scripting.DefaultScriptVariableGenerator;
import org.springframework.integration.scripting.RefreshableResourceScriptSource;
import org.springframework.integration.scripting.ScriptVariableGenerator;
import org.springframework.scripting.ScriptSource;

/**
 * A sink module that routes to one or more named channels.
 *
 * @author Gary Russell
 */
@EnableBinding(Sink.class)
@EnableConfigurationProperties(RouterSinkProperties.class)
@Import(SpelExpressionConverterConfiguration.class)
public class RouterSink {

	@Autowired
	private RouterSinkProperties properties;

	@Autowired
	@Bindings(RouterSink.class)
	Sink channels;

	@Bean
	@ServiceActivator(inputChannel=Sink.INPUT)
	public AbstractMappingMessageRouter router(BinderAwareChannelResolver channelResolver,
			ConfigurableBeanFactory beanFactory) throws Exception {
		AbstractMappingMessageRouter router;
		if (this.properties.getScript() != null) {
			router = new MethodInvokingRouter(scriptProcessor(beanFactory));
		}
		else {
			router = new ExpressionEvaluatingRouter(this.properties.getExpression());
		}
		router.setDefaultOutputChannelName(this.properties.getDefaultOutputChannel());
		router.setResolutionRequired(this.properties.isResolutionRequired());
		Map<String, String> mappings = new LinkedHashMap<String, String>();
		int n = 0;
		for (String value : this.properties.getValues()) {
			mappings.put(value, this.properties.getDestinations()[n++]);
		}
		router.setChannelMappings(mappings);
		router.setChannelResolver(channelResolver);
		return router;
	}

	private GroovyScriptExecutingMessageProcessor scriptProcessor(ConfigurableBeanFactory beanFactory) throws Exception {
		ScriptSource scriptSource = new RefreshableResourceScriptSource(this.properties.getScript(),
				this.properties.getRefreshDelay());
		ScriptModulePropertiesFactoryBean variableProperties = new ScriptModulePropertiesFactoryBean();
		variableProperties.setLocations(this.properties.getPropertiesLocation());
		variableProperties.setVariables(this.properties.getVariables());
		variableProperties.afterPropertiesSet();
		Map<String, Object> variables = new HashMap<>();
		for (Entry<Object, Object> entry : variableProperties.getObject().entrySet()) {
			variables.put((String) entry.getKey(), entry.getValue());
		}
		ScriptVariableGenerator variableGenerator = new DefaultScriptVariableGenerator(variables);
		GroovyScriptExecutingMessageProcessor processor = new GroovyScriptExecutingMessageProcessor(scriptSource,
				variableGenerator);
		processor.setBeanFactory(beanFactory);
		processor.setBeanClassLoader(beanFactory.getBeanClassLoader());
		return processor;
	}

}
