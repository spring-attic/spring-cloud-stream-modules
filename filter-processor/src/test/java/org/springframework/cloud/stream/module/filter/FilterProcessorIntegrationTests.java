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

package org.springframework.cloud.stream.module.filter;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.springframework.cloud.stream.test.matcher.MessageQueueMatcher.receivesPayloadThat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.cloud.stream.annotation.Bindings;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Integration tests for Filter Processor.
 *
 * @author Eric Bottard
 * @author Marius Bogoevici
 * @author Matt Ross
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = FilterProcessorApplication.class)
@WebIntegrationTest(randomPort = true)
@DirtiesContext
public abstract class FilterProcessorIntegrationTests {

	@Autowired
	@Bindings(FilterProcessor.class)
	protected Processor channels;

	@Autowired
	protected MessageCollector collector;

	/**
	 * Validates that the module loads with default properties.
	 */
	public static class UsingNothingIntegrationTests extends FilterProcessorIntegrationTests {

		@Test
		public void test() {
			channels.input().send(new GenericMessage<Object>("hello"));
			channels.input().send(new GenericMessage<Object>("hello world"));
			channels.input().send(new GenericMessage<Object>("hi!"));
			assertThat(collector.forChannel(channels.output()), receivesPayloadThat(is("hello")));
			assertThat(collector.forChannel(channels.output()), receivesPayloadThat(is("hello world")));
			assertThat(collector.forChannel(channels.output()), receivesPayloadThat(is("hi!")));
		}
	}

	@WebIntegrationTest("expression=payload.length()>5")
	public static class UsingExpressionIntegrationTests extends FilterProcessorIntegrationTests {

		@Test
		public void test() throws InterruptedException {
			channels.input().send(new GenericMessage<Object>("hello"));
			channels.input().send(new GenericMessage<Object>("hello world"));
			channels.input().send(new GenericMessage<Object>("hi!"));
			assertThat(collector.forChannel(channels.output()), receivesPayloadThat(is("hello world")));
			assertThat(collector.forChannel(channels.output()).poll(10, MILLISECONDS), is(nullValue(Message.class)));

			channels.input().send(new GenericMessage<Object>("hi!"));
			channels.input().send(new GenericMessage<Object>("hello world"));
			assertThat(collector.forChannel(channels.output()), receivesPayloadThat(not("hi!")));
			assertThat(collector.forChannel(channels.output()).poll(10, MILLISECONDS), is(nullValue(Message.class)));

		}
	}

	@WebIntegrationTest("expression=payload.contains('foo')")
	public static class UsingContainsExpressionIntegrationTests extends FilterProcessorIntegrationTests {

		@Test
		public void test() throws InterruptedException {

			channels.input().send(new GenericMessage<Object>("bazbar"));
			channels.input().send(new GenericMessage<Object>("barbaz"));
			channels.input().send(new GenericMessage<Object>("foobar"));
			assertThat(collector.forChannel(channels.output()), receivesPayloadThat(is("foobar")));
			assertThat(collector.forChannel(channels.output()).poll(10, MILLISECONDS), is(nullValue(Message.class)));

			channels.input().send(new GenericMessage<Object>("''babfoo''"));
			assertThat(collector.forChannel(channels.output()), receivesPayloadThat(is("''babfoo''")));
			assertThat(collector.forChannel(channels.output()).poll(10, MILLISECONDS), is(nullValue(Message.class)));

			channels.input().send(new GenericMessage<Object>("bazbar"));
			channels.input().send(new GenericMessage<Object>("foobar"));
			assertThat(collector.forChannel(channels.output()), receivesPayloadThat(not("bazbar")));
			assertThat(collector.forChannel(channels.output()).poll(10, MILLISECONDS), is(nullValue(Message.class)));
		}
	}

	@WebIntegrationTest("expression=payload=='baz'")
	public static class UsingEqualsExpressionIntegrationTests extends FilterProcessorIntegrationTests {

		@Test
		public void test() throws InterruptedException {

			channels.input().send(new GenericMessage<Object>("baz"));
			channels.input().send(new GenericMessage<Object>("barbaz"));
			channels.input().send(new GenericMessage<Object>("foobar"));
			assertThat(collector.forChannel(channels.output()), receivesPayloadThat(not("foobar")));
            assertThat(collector.forChannel(channels.output()).poll(10, MILLISECONDS), is(nullValue(Message.class)));


            channels.input().send(new GenericMessage<Object>("baz"));
			channels.input().send(new GenericMessage<Object>("foobar"));
			assertThat(collector.forChannel(channels.output()), receivesPayloadThat(not("foobar")));
			assertThat(collector.forChannel(channels.output()).poll(10, MILLISECONDS), is(nullValue(Message.class)));

		}
	}
}
