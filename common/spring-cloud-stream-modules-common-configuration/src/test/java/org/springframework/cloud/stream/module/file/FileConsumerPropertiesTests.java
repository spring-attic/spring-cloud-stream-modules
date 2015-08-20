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

package org.springframework.cloud.stream.module.file;

import org.junit.Test;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.EnvironmentTestUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * @author David Turanski
 */
public class FileConsumerPropertiesTests {

	@Test
	public void fileReadingModeCanBeCustomized() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(context, "mode:ref");
		context.register(Conf.class);
		context.refresh();
		FileConsumerProperties properties = context.getBean(FileConsumerProperties.class);
		assertThat(properties.getMode(), equalTo(FileReadingMode.ref));
	}

	@Test
	public void withMarkersCanBeEnabled() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(context, "mode:lines", "withMarkers:true");
		context.register(Conf.class);
		context.refresh();
		FileConsumerProperties properties = context.getBean(FileConsumerProperties.class);
		assertThat(properties.getMode(), equalTo(FileReadingMode.lines));
		assertTrue(properties.getWithMarkers());
	}

	@Test(expected = BeanCreationException.class)
	public void withMarkersRequiresModeLines() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(context, "withMarkers:true");
		context.register(Conf.class);
		context.refresh();
	}

	@Test(expected = BeanCreationException.class)
	public void invalidModeThrowsException() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(context, "mode:foo");
		context.register(Conf.class);
		context.refresh();
	}

	@Configuration
	@EnableConfigurationProperties(FileConsumerProperties.class)
	static class Conf {
	}
}
