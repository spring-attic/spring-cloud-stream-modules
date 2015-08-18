/*
 * Copyright 2015 the original author or authors.
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

package org.springframework.cloud.stream.module.time;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableModule;
import org.springframework.cloud.stream.annotation.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.support.PeriodicTrigger;

/**
 * @author Dave Syer
 * @author Glenn Renfro
 */
@EnableModule(Source.class)
@EnableConfigurationProperties(TimeSourceProperties.class)
public class TimeSource {

	@Autowired
	private TimeSourceProperties properties;

	@Bean
	public Trigger trigger() {
		PeriodicTrigger trigger = new PeriodicTrigger(properties.getFixedDelay(), TimeUnit.valueOf(properties.getTimeUnit()));
		trigger.setInitialDelay(properties.getInitialDelay());
		return trigger;
	}

	@InboundChannelAdapter(value = Source.OUTPUT, poller = @Poller(trigger = "trigger", maxMessagesPerPoll = "1"))
	public String publishTime() {
		return new SimpleDateFormat(this.properties.getFormat()).format(new Date());
	}

}
