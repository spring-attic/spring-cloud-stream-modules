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
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.springframework.cloud.stream.test.matcher.MessageQueueMatcher.*;

import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.cloud.stream.annotation.ModuleChannels;
import org.springframework.cloud.stream.annotation.Processor;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Integration Tests for GroovyFilterProcessor.
 *
 * @author Eric Bottard
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = GroovyFilterProcessorApplication.class)
@WebIntegrationTest(randomPort = true)
@DirtiesContext
public abstract class GroovyFilterProcessorApplicationTests {

	@Autowired
	@ModuleChannels(GroovyFilterProcessor.class)
	protected Processor channels;

	@Autowired
	protected MessageCollector collector;

	@WebIntegrationTest({"script=script.groovy", "variables=threshold=5"})
	public static class UsingScriptTests extends GroovyFilterProcessorApplicationTests {

		@Test
		public void test() throws InterruptedException {
			channels.input().send(new GenericMessage<Object>("hello"));
			channels.input().send(new GenericMessage<Object>("hello world"));
			channels.input().send(new GenericMessage<Object>("hi!"));
			assertThat(collector.forChannel(channels.output()), receivesPayloadThat(is("hello world")));
			assertThat(collector.forChannel(channels.output()).poll(10, MILLISECONDS), is(nullValue(Message.class)));
		}

	}
}
