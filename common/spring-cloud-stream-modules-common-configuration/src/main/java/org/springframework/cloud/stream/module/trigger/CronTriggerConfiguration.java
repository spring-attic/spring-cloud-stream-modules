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
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.support.CronTrigger;

/**
 * Configuration class for Cron trigger.
 *
 * @author Ilayaperumal Gopinathan
 */
@Configuration
@EnableConfigurationProperties(CronTriggerProperties.class)
@ConditionalOnProperty("cron")
public class CronTriggerConfiguration {

	@Autowired
	CronTriggerProperties cronTriggerProperties;

	@Bean(name = TriggerConstants.TRIGGER_BEAN_NAME)
	Trigger cronTrigger() {
		return new CronTrigger(cronTriggerProperties.getCron());
	}

}
