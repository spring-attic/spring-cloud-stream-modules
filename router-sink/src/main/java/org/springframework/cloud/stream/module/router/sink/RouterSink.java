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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.Bindings;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.binding.BinderAwareChannelResolver;
import org.springframework.cloud.stream.config.SpelExpressionConverterConfiguration;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.cloud.stream.module.common.ScriptVariableGeneratorConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.groovy.GroovyScriptExecutingMessageProcessor;
import org.springframework.integration.router.AbstractMappingMessageRouter;
import org.springframework.integration.router.ExpressionEvaluatingRouter;
import org.springframework.integration.router.MethodInvokingRouter;
import org.springframework.integration.scripting.RefreshableResourceScriptSource;
import org.springframework.integration.scripting.ScriptVariableGenerator;
import org.springframework.scripting.ScriptSource;

/**
 * A sink module that routes to one or more named channels.
 * @author Gary Russell
 */
@EnableBinding(Sink.class)
@Import({SpelExpressionConverterConfiguration.class, ScriptVariableGeneratorConfiguration.class})
@EnableConfigurationProperties(RouterSinkProperties.class)
public class RouterSink {

	@Autowired
	private RouterSinkProperties properties;

	@Autowired
	private ScriptVariableGenerator scriptVariableGenerator;

	@Autowired
	@Bindings(RouterSink.class)
	Sink channels;

	@Bean
	@ServiceActivator(inputChannel = Sink.INPUT)
	public AbstractMappingMessageRouter router(BinderAwareChannelResolver channelResolver) {
		AbstractMappingMessageRouter router;
		if (this.properties.getScript() != null) {
			router = new MethodInvokingRouter(scriptProcessor());
		}
		else {
			router = new ExpressionEvaluatingRouter(this.properties.getExpression());
		}
		router.setDefaultOutputChannelName(this.properties.getDefaultOutputChannel());
		router.setResolutionRequired(this.properties.isResolutionRequired());
		if (this.properties.getDestinationMappings() != null) {
			router.replaceChannelMappings(this.properties.getDestinationMappings());
		}
		router.setChannelResolver(channelResolver);
		return router;
	}

	@Bean
	@ConditionalOnProperty("script")
	public GroovyScriptExecutingMessageProcessor scriptProcessor() {
		ScriptSource scriptSource = new RefreshableResourceScriptSource(this.properties.getScript(),
				this.properties.getRefreshDelay());
		return new GroovyScriptExecutingMessageProcessor(scriptSource, this.scriptVariableGenerator);
	}

}
