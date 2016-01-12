/*
 * Copyright 2016 the original author or authors.
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

package org.springframework.cloud.stream.module.jdbc.source;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.EnvironmentTestUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Thomas Risberg
 */
public class JdbcSourcePropertiesTests {

	@Rule
	public ExpectedException thrown= ExpectedException.none();

	@Test
	public void queryIsRequired() {
		thrown.expect(BeanCreationException.class);
		thrown.expectMessage("Field error in object 'target' on field 'query': rejected value [null]");
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.register(Conf.class);
		context.refresh();
	}

	@Test
	public void queryCanBeCustomized() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		String query = "select foo from bar";
		EnvironmentTestUtils.addEnvironment(context, "query:" + query);
		context.register(Conf.class);
		context.refresh();
		JdbcSourceProperties properties = context.getBean(JdbcSourceProperties.class);
		assertThat(properties.getQuery(), equalTo(query));
	}

	@Test
	public void updateCanBeCustomized() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(context, "query:select foo from bar where baz < 1");
		String update = "update bar set baz=1 where foo in (:foo)";
		EnvironmentTestUtils.addEnvironment(context, "update:" + update);
		context.register(Conf.class);
		context.refresh();
		JdbcSourceProperties properties = context.getBean(JdbcSourceProperties.class);
		assertThat(properties.getUpdate(), equalTo(update));
	}

	@Test
	public void splitDefaultsToTrue() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(context, "query:select foo from bar");
		context.register(Conf.class);
		context.refresh();
		JdbcSourceProperties properties = context.getBean(JdbcSourceProperties.class);
		assertThat(properties.isSplit(), equalTo(true));
	}

	@Test
	public void splitCanBeCustomized() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(context, "query:select foo from bar");
		EnvironmentTestUtils.addEnvironment(context, "split:false");
		context.register(Conf.class);
		context.refresh();
		JdbcSourceProperties properties = context.getBean(JdbcSourceProperties.class);
		assertThat(properties.isSplit(), equalTo(false));
	}

	@Test
	public void fixedDelayDefaultsTo5() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(context, "query:select foo from bar");
		context.register(Conf.class);
		context.refresh();
		JdbcSourceProperties properties = context.getBean(JdbcSourceProperties.class);
		assertThat(properties.getFixedDelay(), equalTo(5));
	}

	@Test
	public void fixedDelayCanBeCustomized() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(context, "query:select foo from bar");
		EnvironmentTestUtils.addEnvironment(context, "fixedDelay:10");
		context.register(Conf.class);
		context.refresh();
		JdbcSourceProperties properties = context.getBean(JdbcSourceProperties.class);
		assertThat(properties.getFixedDelay(), equalTo(10));
	}

	@Test
	public void fixedDelayNotLessThan1() {
		thrown.expect(BeanCreationException.class);
		thrown.expectMessage("Field error in object 'target' on field 'fixedDelay': rejected value [0]");
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(context, "query:select foo from bar");
		EnvironmentTestUtils.addEnvironment(context, "fixedDelay:0");
		context.register(Conf.class);
		context.refresh();
	}

	@Test
	public void maxRowsPerPollCanBeCustomized() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(context, "query:select foo from bar");
		EnvironmentTestUtils.addEnvironment(context, "maxRowsPerPoll:15");
		context.register(Conf.class);
		context.refresh();
		JdbcSourceProperties properties = context.getBean(JdbcSourceProperties.class);
		assertThat(properties.getMaxRowsPerPoll(), equalTo(15));
	}

	@Test
	public void maxMessagesCanBeCustomized() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(context, "query:select foo from bar");
		EnvironmentTestUtils.addEnvironment(context, "maxMessages:-1");
		context.register(Conf.class);
		context.refresh();
		JdbcSourceProperties properties = context.getBean(JdbcSourceProperties.class);
		assertThat(properties.getMaxMessages(), equalTo(-1L));
	}

	@Configuration
	@EnableConfigurationProperties(JdbcSourceProperties.class)
	static class Conf {
	}
}
