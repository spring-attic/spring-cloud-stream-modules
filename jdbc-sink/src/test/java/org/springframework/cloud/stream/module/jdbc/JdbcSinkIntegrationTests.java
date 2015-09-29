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

package org.springframework.cloud.stream.module.jdbc;

import static org.hamcrest.Matchers.*;

import java.util.Arrays;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.EmbeddedDataSourceConfiguration;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.cloud.stream.annotation.Bindings;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Integration Tests for JdbcSink. Uses hsqldb as a (real) embedded DB.
 *
 * @author Eric Bottard
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {JdbcSinkApplication.class, EmbeddedDataSourceConfiguration.class})
@IntegrationTest({"server.port=-1"})
@DirtiesContext
public abstract class JdbcSinkIntegrationTests {

	@Autowired
	@Bindings(JdbcSinkConfiguration.class)
	protected Sink channels;

	@Autowired
	protected JdbcOperations jdbcOperations;

	@IntegrationTest
	public static class DefaultBehavior extends JdbcSinkIntegrationTests {

		@Test
		public void testInsertion() {
			Payload sent = new Payload("hello", 42);
			channels.input().send(MessageBuilder.withPayload(sent).build());
			String result = jdbcOperations.queryForObject("select payload from messages", String.class);
			Assert.assertThat(result, is("hello42"));
		}
	}

	@IntegrationTest(value = "columns=a,b")
	public static class SimpleMappingTests extends JdbcSinkIntegrationTests {

		@Test
		public void testInsertion() {
			Payload sent = new Payload("hello", 42);
			channels.input().send(MessageBuilder.withPayload(sent).build());
			Payload result = jdbcOperations.query("select a, b from messages", new BeanPropertyRowMapper<>(Payload.class)).get(0);
			Assert.assertThat(result, Matchers.samePropertyValuesAs(sent));
		}
	}

	// annotation below relies on java.util.Properties so backslash needs to be doubled
	@IntegrationTest(value = "columns=a: a.substring(0\\\\, 4), b: b + 624")
	public static class SpELTests extends JdbcSinkIntegrationTests {

		@Test
		public void testInsertion() {
			Payload sent = new Payload("hello", 42);
			channels.input().send(MessageBuilder.withPayload(sent).build());
			Payload expected = new Payload("hell", 666);
			Payload result = jdbcOperations.query("select a, b from messages", new BeanPropertyRowMapper<>(Payload.class)).get(0);

			Assert.assertThat(result, Matchers.samePropertyValuesAs(expected));
		}
	}

	@IntegrationTest(value = {"batchSize=3", "columns=a,b"})
	public static class BatchingTests extends JdbcSinkIntegrationTests {

		@Test
		public void testInsertion() {
			Payload a = new Payload("hello", 42);
			Payload b = new Payload("world", 12);
			Payload c = new Payload("bonjour", 32);
			Payload d = new Payload("monde", 22);
			channels.input().send(MessageBuilder.withPayload(Arrays.asList(a, b, c, d)).build());
			List<Payload> result = jdbcOperations.query("select a, b from messages", new BeanPropertyRowMapper<>(Payload.class));
			Assert.assertThat(result, containsInAnyOrder(
					samePropertyValuesAs(a),
					samePropertyValuesAs(b),
					samePropertyValuesAs(c),
					samePropertyValuesAs(d)
			));
		}
	}
	@IntegrationTest(value = {"tableName=no_script", "initialize=true", "columns=a,b"})
	public static class ImplicitTableCreationTests extends JdbcSinkIntegrationTests {

		@Test
		public void testInsertion() {
			Payload sent = new Payload("hello", 42);
			channels.input().send(MessageBuilder.withPayload(sent).build());
			Payload result = jdbcOperations.query("select a, b from no_script", new BeanPropertyRowMapper<>(Payload.class)).get(0);
			Assert.assertThat(result, Matchers.samePropertyValuesAs(sent));
		}
	}

	@IntegrationTest(value = {"tableName=foobar", "initialize=classpath:explicit-script.sql", "columns=a,b"})
	public static class ExlicitTableCreationTests extends JdbcSinkIntegrationTests {

		@Test
		public void testInsertion() {
			Payload sent = new Payload("hello", 42);
			channels.input().send(MessageBuilder.withPayload(sent).build());
			Payload result = jdbcOperations.query("select a, b from foobar", new BeanPropertyRowMapper<>(Payload.class)).get(0);
			Assert.assertThat(result, Matchers.samePropertyValuesAs(sent));
		}
	}

	public static class Payload {
		private String a;

		private int b;

		public Payload() {
		}

		public Payload(String a, int b) {
			this.a = a;
			this.b = b;
		}

		public String getA() {
			return a;
		}

		public int getB() {
			return b;
		}

		public void setA(String a) {
			this.a = a;
		}

		public void setB(int b) {
			this.b = b;
		}

		@Override
		public String toString() {
			return a + b;
		}
	}

}
