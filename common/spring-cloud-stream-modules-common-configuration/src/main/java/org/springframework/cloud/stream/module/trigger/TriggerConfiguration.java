/*
 * Copyright 2016 the original author or authors.
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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.NoneNestedConditions;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.module.MaxMessagesProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.core.Pollers;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.PeriodicTrigger;

/**
 * @author David Turanski
 * @author Ilayaperumal Gopinathan
 */
@Configuration
@EnableConfigurationProperties({TriggerProperties.class, MaxMessagesProperties.class})
public class TriggerConfiguration {

	@Autowired
	TriggerProperties config;

	@Autowired
	private MaxMessagesProperties maxMessagesProperties;

	@Bean
	public PollerMetadata defaultPoller(Trigger trigger) {
		return Pollers.trigger(trigger).maxMessagesPerPoll(this.maxMessagesProperties.getMaxMessages()).get();
	}

	@Bean(name = TriggerConstants.TRIGGER_BEAN_NAME)
	@ConditionalOnProperty(TriggerConstants.CRON_TRIGGER_OPTION)
	public Trigger cronTrigger() {
		return new CronTrigger(config.getCron());
	}

	@Bean(name = TriggerConstants.TRIGGER_BEAN_NAME)
	@Conditional(PeriodicTriggerCondition.class)
	public Trigger periodicTrigger() {
		PeriodicTrigger trigger = new PeriodicTrigger(config.getFixedDelay(),
				config.getTimeUnit());
		trigger.setInitialDelay(config.getInitialDelay());
		return trigger;
	}

	@Bean(name = TriggerConstants.TRIGGER_BEAN_NAME)
	@ConditionalOnProperty(TriggerConstants.DATE_TRIGGER_OPTION)
	public Trigger dateTrigger() {
		return new DateTrigger(config.getDate());
	}

	static class PeriodicTriggerCondition extends NoneNestedConditions {

		PeriodicTriggerCondition() {
			super(ConfigurationPhase.PARSE_CONFIGURATION);
		}

		@ConditionalOnProperty(TriggerConstants.CRON_TRIGGER_OPTION)
		static class cronTrigger {
		}

		@ConditionalOnProperty(TriggerConstants.DATE_TRIGGER_OPTION)
		static class dateTrigger {
		}

	}
}
