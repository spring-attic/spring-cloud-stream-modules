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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.NoneNestedConditions;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.PeriodicTrigger;

/**
 * @author David Turanski
 * @author Ilayaperumal Gopinathan
 */
@Configuration
@EnableConfigurationProperties(TriggerProperties.class)
public class TriggerConfiguration {

	@Autowired
	TriggerProperties config;

	@Bean(name = TriggerConstants.TRIGGER_BEAN_NAME)
	@ConditionalOnProperty(TriggerConstants.CRON_TRIGGER_OPTION)
	Trigger cronTrigger() {
		return new CronTrigger(config.getCron());
	}

	@Bean(name = TriggerConstants.TRIGGER_BEAN_NAME)
	@Conditional(PeriodicTriggerCondition.class)
	Trigger periodicTrigger() {
		PeriodicTrigger trigger = new PeriodicTrigger(config.getFixedDelay(),
				config.getTimeUnit());
		trigger.setInitialDelay(config.getInitialDelay());
		return trigger;
	}

	@Bean(name = TriggerConstants.TRIGGER_BEAN_NAME)
	@ConditionalOnProperty(TriggerConstants.DATE_TRIGGER_OPTION)
	Trigger dateTrigger() {
		Date date;
		DateFormat dateFormat = new SimpleDateFormat(config.getDateFormat());
		String dateString = config.getDate() != null ? config.getDate() :
				new Date().toString();
		try {
			date = dateFormat.parse(dateString);
		}
		catch (ParseException pe) {
			throw new BeanCreationException("Exception parsing the Date format: " + config.getDateFormat(), pe);
		}
		return new DateTrigger(date);
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
