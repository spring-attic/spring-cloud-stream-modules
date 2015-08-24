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

package org.springframework.cloud.stream.module;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.support.PeriodicTrigger;

/**
 * @author David Turanski
 */
@Configuration
@EnableConfigurationProperties(PeriodicTriggerProperties.class)
public class PeriodicTriggerConfiguration {
	
	public static final String TRIGGER_BEAN_NAME = "periodicTrigger";

	@Autowired
	PeriodicTriggerProperties config;

	@Bean(name = TRIGGER_BEAN_NAME)
	PeriodicTrigger periodicTrigger() {
		PeriodicTrigger trigger = new PeriodicTrigger(config.getFixedDelay(),
				config.getTimeUnit());
		trigger.setInitialDelay(config.getInitialDelay());
		return trigger;
	}
}
