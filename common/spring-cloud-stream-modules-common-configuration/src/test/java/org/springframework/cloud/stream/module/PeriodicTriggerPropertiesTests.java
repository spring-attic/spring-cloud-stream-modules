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

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.EnvironmentTestUtils;
import org.springframework.cloud.stream.module.trigger.TriggerProperties;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author David Turanski
 */
public class PeriodicTriggerPropertiesTests {

	@Test
	public void fixedDelayCanBeCustomized() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(context, "fixedDelay:11");
		context.register(Conf.class);
		context.refresh();
		TriggerProperties properties = context.getBean(TriggerProperties
				.class);
		assertThat(properties.getFixedDelay(), equalTo(11));
	}

	@Test
	public void initialDelayCanBeCustomized() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(context, "initialDelay:12");
		context.register(Conf.class);
		context.refresh();
		TriggerProperties properties = context.getBean(TriggerProperties
				.class);
		assertThat(properties.getInitialDelay(), equalTo(12));
	}

	@Test
	public void timeUnitCanBeCustomized() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(context, "timeUnit:MINUTES");
		context.register(Conf.class);
		context.refresh();
		TriggerProperties properties = context.getBean(TriggerProperties
				.class);
		assertThat(properties.getTimeUnit(), equalTo(TimeUnit.MINUTES));
	}

	@Test(expected = BeanCreationException.class)
	public void invalidTimeUnitThrowsException() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(context, "timeUnit:HALF_HOUR");
		context.register(Conf.class);
		context.refresh();
	}

	@Configuration
	@EnableConfigurationProperties(TriggerProperties.class)
	static class Conf {
	}
}
