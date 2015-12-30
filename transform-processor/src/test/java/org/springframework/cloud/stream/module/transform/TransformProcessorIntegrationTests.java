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

package org.springframework.cloud.stream.module.transform;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.springframework.cloud.stream.test.matcher.MessageQueueMatcher.receivesPayloadThat;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.cloud.stream.annotation.Bindings;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Integration Tests for the Transform Processor.
 *
 * @author Eric Bottard
 * @author Marius Bogoevici
 * @author Matt Ross
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = TransformProcessorApplication.class)
@WebIntegrationTest(randomPort = true)
@DirtiesContext
public abstract class TransformProcessorIntegrationTests {

	@Autowired
	@Bindings(TransformProcessor.class)
	protected Processor channels;

	@Autowired
	protected MessageCollector collector;

	/**
	 * Validates that the module loads with default properties.
	 */
	public static class UsingNothingIntegrationTests extends TransformProcessorIntegrationTests {

		@Test
		public void test() {
			channels.input().send(new GenericMessage<Object>("hello"));
			assertThat(collector.forChannel(channels.output()), receivesPayloadThat(is("hello")));
		}
	}

	@WebIntegrationTest("expression=payload.toUpperCase()")
	public static class UsingUpperCaseExpressionIntegrationTests extends TransformProcessorIntegrationTests {

		@Test
		public void test() {
			channels.input().send(new GenericMessage<Object>("hello"));
			assertThat(collector.forChannel(channels.output()), receivesPayloadThat(is("HELLO")));
		}
	}

	@WebIntegrationTest("expression=payload.toLowerCase()")
	public static class UsingLowerCaseExpressionIntegrationTests extends TransformProcessorIntegrationTests {

		@Test
		public void test() {
			channels.input().send(new GenericMessage<Object>("GOODBYE"));
			assertThat(collector.forChannel(channels.output()), receivesPayloadThat(is("goodbye")));

			channels.input().send(new GenericMessage<Object>("badBadNotGOOD"));
			assertThat(collector.forChannel(channels.output()), receivesPayloadThat(is("badbadnotgood")));
		}
	}

	@WebIntegrationTest("expression=payload.trim()")
	public static class UsingTrimExpressionIntegrationTests extends TransformProcessorIntegrationTests {

		@Test
		public void test() {
			channels.input().send(new GenericMessage<Object>("  monday tuesday friday   "));
			assertThat(collector.forChannel(channels.output()), receivesPayloadThat(is("monday tuesday friday")));
		}
	}

	@WebIntegrationTest("expression=payload.substring(3)")
	public static class UsingSubStringExpressionIntegrationTests extends TransformProcessorIntegrationTests {

		@Test
		public void test() {
			channels.input().send(new GenericMessage<Object>("FOOBARBAZ"));
			assertThat(collector.forChannel(channels.output()), receivesPayloadThat(is("BARBAZ")));

			channels.input().send(new GenericMessage<Object>("FOO"));
			assertThat(collector.forChannel(channels.output()), receivesPayloadThat(is("")));
		}
	}

	@WebIntegrationTest("expression=payload.replaceAll('i','!')")
	public static class UsingReplaceExpressionIntegrationTests extends TransformProcessorIntegrationTests {

		@Test
		public void test() {
			channels.input().send(new GenericMessage<Object>("in the distant past"));
			assertThat(collector.forChannel(channels.output()), receivesPayloadThat(is("!n the d!stant past")));
		}
	}

    @WebIntegrationTest("expression=payload.concat('.io')")
    public static class UsingConcatExpressionIntegrationTests extends TransformProcessorIntegrationTests {

        @Test
        public void test() {
            channels.input().send(new GenericMessage<Object>("docs.spring"));
            assertThat(collector.forChannel(channels.output()), receivesPayloadThat(is("docs.spring.io")));
        }
    }
}
