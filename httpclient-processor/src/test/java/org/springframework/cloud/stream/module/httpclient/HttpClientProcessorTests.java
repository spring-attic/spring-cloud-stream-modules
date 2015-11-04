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

package org.springframework.cloud.stream.module.httpclient;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.springframework.cloud.stream.test.matcher.MessageQueueMatcher.receivesPayloadThat;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Tests for Http Client Processor.
 *
 * @author Waldemar Hummer
 * @author Mark Fisher
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = HttpClientProcessorApplication.class)
@WebIntegrationTest(randomPort = true)
@DirtiesContext
public abstract class HttpClientProcessorTests {

	@Autowired
	protected Processor channels;

	@Autowired
	protected MessageCollector messageCollector;

	@WebIntegrationTest(value = "url=http://google.com", randomPort = true)
	public static class TestRequestGET extends HttpClientProcessorTests {

		@Test
		public void testRequest() {
			channels.input().send(new GenericMessage<Object>("hello"));
			assertThat(messageCollector.forChannel(channels.output()), receivesPayloadThat(containsString("google")));
		}

	}

	@WebIntegrationTest(value = "urlExpression='http://google.com'", randomPort = true)
	public static class TestRequestGETWithUrlExpression extends HttpClientProcessorTests {

		@Test
		public void testRequest() {
			channels.input().send(new GenericMessage<Object>("hello"));
			assertThat(messageCollector.forChannel(channels.output()), receivesPayloadThat(containsString("google")));
		}

	}

	@WebIntegrationTest(
			value = {
				"url=https://httpbin.org/post",
				"body={\"foo\":\"bar\"}",
				"httpMethod=POST"},
			randomPort = true)
	public static class TestRequestPOST extends HttpClientProcessorTests {

		@Test
		public void testRequest() {
			channels.input().send(new GenericMessage<Object>("..."));
			assertThat(messageCollector.forChannel(channels.output()), 
					receivesPayloadThat(Matchers.allOf(
							containsString("foo"), containsString("bar"))));
		}

	}

	@WebIntegrationTest(
			value = {
				"url=https://httpbin.org/post",
				"bodyExpression=payload",
				"httpMethod=POST"},
			randomPort = true)
	public static class TestRequestPOSTWithBodyExpression extends HttpClientProcessorTests {

		@Test
		public void testRequest() {
			channels.input().send(new GenericMessage<Object>("{\"foo\":\"bar\"}"));
			assertThat(messageCollector.forChannel(channels.output()), 
					receivesPayloadThat(Matchers.allOf(
							containsString("foo"), containsString("bar"))));
		}

	}

	@WebIntegrationTest(
			value = {
				"url=https://httpbin.org/headers",
				"headersExpression={Key1:'value1',Key2:'value2'}"},
			randomPort = true)
	public static class TestRequestWithHeaders extends HttpClientProcessorTests {

		@Test
		public void testRequest() {
			channels.input().send(new GenericMessage<Object>("..."));
			assertThat(messageCollector.forChannel(channels.output()), 
					receivesPayloadThat(Matchers.allOf(
							containsString("Key1"), containsString("value1"),
							containsString("Key2"), containsString("value2"))));
		}

	}

	@WebIntegrationTest(
			value = {
				"url=http://google.com",
				"expectedReturnType=byte[]"},
			randomPort = true)
	public static class TestRequestWithReturnType extends HttpClientProcessorTests {

		@Test
		public void testRequest() {
			channels.input().send(new GenericMessage<Object>("hello"));
			assertThat(messageCollector.forChannel(channels.output()), 
					receivesPayloadThat(Matchers.isA(byte[].class)));
		}

	}

}
