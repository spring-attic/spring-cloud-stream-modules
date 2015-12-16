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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.NoneNestedConditions;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ConfigurationCondition;
import org.springframework.scheduling.support.PeriodicTrigger;

/**
 * @author David Turanski
 * @author Ilayaperumal Gopinathan
 */
@Configuration
@EnableConfigurationProperties(PeriodicTriggerProperties.class)
@Conditional(PeriodicTriggerConfiguration.PeriodicTriggerCondition.class)
public class PeriodicTriggerConfiguration {

	@Autowired
	PeriodicTriggerProperties config;

	@Bean(name = TriggerConstants.TRIGGER_BEAN_NAME)
	PeriodicTrigger periodicTrigger() {
		PeriodicTrigger trigger = new PeriodicTrigger(config.getFixedDelay(),
				config.getTimeUnit());
		trigger.setInitialDelay(config.getInitialDelay());
		return trigger;
	}

	static class PeriodicTriggerCondition extends NoneNestedConditions {

		PeriodicTriggerCondition() {
			super(ConfigurationCondition.ConfigurationPhase.PARSE_CONFIGURATION);
		}

		@ConditionalOnProperty(TriggerConstants.CRON_TRIGGER_OPTION)
		static class cronTrigger {
		}

		@ConditionalOnProperty(TriggerConstants.DATE_TRIGGER_OPTION)
		static class dateTrigger {
		}

	}
}
