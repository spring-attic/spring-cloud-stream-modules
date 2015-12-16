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
import org.springframework.cloud.stream.config.SpelExpressionConverterConfiguration;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Import;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;

/**
 * @author Ilayaperumal Gopinathan
 */
@EnableBinding(Source.class)
@EnableConfigurationProperties(TriggerSourceProperties.class)
@Import({PeriodicTriggerConfiguration.class, CronTriggerConfiguration.class, DateTriggerConfiguration.class,
		SpelExpressionConverterConfiguration.class})
public class TriggerSource {

	@Autowired
	private EvaluationContext evaluationContext;

	@Autowired
	private TriggerSourceProperties config;

	@InboundChannelAdapter(value = Source.OUTPUT, poller = @Poller(
			trigger = TriggerConstants.TRIGGER_BEAN_NAME, maxMessagesPerPoll = "1"), autoStartup = "false")
	public String trigger() {
		if (this.config.getPayload() != null && !this.config.getPayload().isEmpty()) {
			return new LiteralExpression(this.config.getPayload()).getValue(this.evaluationContext, String.class);
		}
		// Return empty string as payload
		return "";
	}

}
