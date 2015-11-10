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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.springframework.cloud.stream.test.matcher.MessageQueueMatcher.*;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Tests for Http Client Processor.
 *
 * @author Eric Bottard
 * @author Waldemar Hummer
 * @author Mark Fisher
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {HttpClientProcessorApplication.class, HttpClientProcessorTests.AdditionalController.class})
@IntegrationTest("server.port=9494") // Need to use a hardcoded port to be able to reference it below
@DirtiesContext
public abstract class HttpClientProcessorTests {

	@Autowired
	protected Processor channels;

	@Autowired
	protected MessageCollector messageCollector;


	@WebIntegrationTest(value = {"url=http://localhost:9494/health"})
	public static class TestRequestGET extends HttpClientProcessorTests {

		@Test
		public void testRequest() {
			channels.input().send(new GenericMessage<Object>("hello"));
			assertThat(messageCollector.forChannel(channels.output()), receivesPayloadThat(containsString("status")));
		}

	}

	@WebIntegrationTest(value = "urlExpression='http://localhost:9494/'+payload")
	public static class TestRequestGETWithUrlExpression extends HttpClientProcessorTests {

		@Test
		public void testRequest() {
			channels.input().send(new GenericMessage<Object>("health"));
			assertThat(messageCollector.forChannel(channels.output()), receivesPayloadThat(containsString("status")));
		}

	}

	@WebIntegrationTest(
			value = {
					"url=http://localhost:9494/foobar",
					"body={\"foo\":\"bar\"}",
					"httpMethod=POST"})
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
					"url=http://localhost:9494/foobar",
					"httpMethod=POST"})
	public static class TestRequestPOSTWithBodyExpression extends HttpClientProcessorTests {

		@Test
		public void testRequest() {
			channels.input().send(new GenericMessage<Object>("{\"foo\":\"bar\"}"));
			assertThat(messageCollector.forChannel(channels.output()),
					receivesPayloadThat(Matchers.allOf(containsString("Hello"),
							containsString("foo"), containsString("bar"))));
		}

	}

	@WebIntegrationTest(
			value = {
					"url=http://localhost:9494/headers",
					"headersExpression={Key1:'value1',Key2:'value2'}"})
	public static class TestRequestWithHeaders extends HttpClientProcessorTests {

		@Test
		public void testRequest() {
			channels.input().send(new GenericMessage<Object>("..."));
			assertThat(messageCollector.forChannel(channels.output()),
					receivesPayloadThat(is("value1 value2")));
		}

	}

	@WebIntegrationTest(
			value = {
					"url=http://localhost:9494/foobar",
					"httpMethod=POST",
					"headersExpression={Accept:'application/octet-stream'}",
					"expectedResponseType=byte[]"})
	public static class TestRequestWithReturnType extends HttpClientProcessorTests {

		@Test
		public void testRequest() {
			channels.input().send(new GenericMessage<Object>("hello"));
			assertThat(messageCollector.forChannel(channels.output()),
					receivesPayloadThat(Matchers.isA(byte[].class)));
		}

	}

	@WebIntegrationTest(
			value = {
					"url=http://localhost:9494/foobar",
					"httpMethod=POST",
					"resultExpression=body.substring(3,8)"})
	public static class TestRequestWithResultExtractor extends HttpClientProcessorTests {

		@Test
		public void testRequest() {
			channels.input().send(new GenericMessage<Object>("hi"));
			assertThat(messageCollector.forChannel(channels.output()),
					receivesPayloadThat(is("lo hi")));
		}

	}

	@RestController
	public static class AdditionalController {

		@RequestMapping("/foobar")
		public String greet(@RequestBody String who) {
			return "Hello " + who;
		}

		@RequestMapping("/headers")
		public String headers(@RequestHeader("Key1") String key1, @RequestHeader("Key2") String key2) {
			return key1 + " " + key2;
		}

	}

}
