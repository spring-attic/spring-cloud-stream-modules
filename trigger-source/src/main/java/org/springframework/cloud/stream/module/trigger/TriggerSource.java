/*
 * Copyright 2015 the original author or authors.
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
package org.springframework.cloud.stream.module.trigger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.cloud.stream.module.MaxMessagesProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.expression.EvaluationContext;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.dsl.core.Pollers;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.scheduling.Trigger;

/**
 * @author Ilayaperumal Gopinathan
 */
@EnableBinding(Source.class)
@EnableConfigurationProperties({TriggerSourceProperties.class, MaxMessagesProperties.class})
@Import({TriggerConfiguration.class})
public class TriggerSource {

	@Autowired
	private EvaluationContext evaluationContext;

	@Autowired
	private TriggerSourceProperties config;

	@Autowired
	private MaxMessagesProperties maxMessagesProperties;

	@Autowired
	private Trigger trigger;

	@Bean
	public PollerMetadata poller() {
		return Pollers.trigger(trigger).maxMessagesPerPoll(this.maxMessagesProperties.getMaxMessages()).get();
	}

	@InboundChannelAdapter(value = Source.OUTPUT, poller = @Poller("poller"), autoStartup = "false")
	public String trigger() {
		return this.config.getPayload().getValue(this.evaluationContext, String.class);
	}

}
