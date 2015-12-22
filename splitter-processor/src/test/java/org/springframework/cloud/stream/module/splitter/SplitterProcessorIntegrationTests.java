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

package org.springframework.cloud.stream.module.splitter;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.springframework.cloud.stream.test.matcher.MessageQueueMatcher.receivesPayloadThat;
import static org.springframework.integration.test.matcher.HeaderMatcher.hasSequenceNumber;
import static org.springframework.integration.test.matcher.HeaderMatcher.hasSequenceSize;
import static org.springframework.integration.test.matcher.PayloadMatcher.hasPayload;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.hamcrest.Description;
import org.hamcrest.DiagnosingMatcher;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.cloud.stream.annotation.Bindings;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.integration.IntegrationMessageHeaderAccessor;
import org.springframework.integration.endpoint.EventDrivenConsumer;
import org.springframework.integration.file.splitter.FileSplitter;
import org.springframework.integration.file.splitter.FileSplitter.FileMarker;
import org.springframework.integration.file.splitter.FileSplitter.FileMarker.Mark;
import org.springframework.integration.splitter.AbstractMessageSplitter;
import org.springframework.integration.splitter.DefaultMessageSplitter;
import org.springframework.integration.splitter.ExpressionEvaluatingSplitter;
import org.springframework.integration.test.util.TestUtils;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Integration Tests for the Splitter Processor.
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = SplitterProcessorApplication.class)
@WebIntegrationTest(randomPort = true)
@DirtiesContext
public abstract class SplitterProcessorIntegrationTests {

	@Autowired
	@Bindings(SplitterProcessor.class)
	protected Processor channels;

	@Autowired
	protected MessageCollector collector;

	@Autowired
	protected AbstractMessageSplitter splitter;

	@Autowired
	@Qualifier("splitterProcessor.splitterHandler.splitter")
	protected EventDrivenConsumer consumer;

	/**
	 * Validates that the module loads with default properties.
	 */
	public static class UsingNothingIntegrationTests extends SplitterProcessorIntegrationTests {

		@Test
		public void test() {
			assertThat(this.splitter, instanceOf(DefaultMessageSplitter.class));
			assertSame(this.splitter, TestUtils.getPropertyValue(this.consumer, "handler"));
			channels.input().send(new GenericMessage<Object>(Arrays.asList(new String[] { "hello", "world" })));
			assertThat(collector.forChannel(channels.output()), receivesPayloadThat(is("hello")));
			assertThat(collector.forChannel(channels.output()), receivesPayloadThat(is("world")));
		}
	}

	@IntegrationTest("delimiters = ,")
	public static class withDelimitersTests extends SplitterProcessorIntegrationTests {

		@Test
		public void test() {
			assertThat(this.splitter, instanceOf(DefaultMessageSplitter.class));
			assertSame(this.splitter, TestUtils.getPropertyValue(this.consumer, "handler"));
			channels.input().send(new GenericMessage<Object>("hello,world"));
			assertThat(collector.forChannel(channels.output()), receivesPayloadThat(is("hello")));
			assertThat(collector.forChannel(channels.output()), receivesPayloadThat(is("world")));
		}
	}

	@IntegrationTest({ "fileMarkers = false", "charset = UTF-8", "applySequence = false" })
	public static class fromFileTests extends SplitterProcessorIntegrationTests {

		@Test
		public void test() throws Exception {
			assertThat(this.splitter, instanceOf(FileSplitter.class));
			assertSame(this.splitter, TestUtils.getPropertyValue(this.consumer, "handler"));
			File file = new File(System.getProperty("java.io.tmpdir") + File.separator + "splitter.proc.test");
			FileOutputStream fos = new FileOutputStream(file);
			fos.write("hello\nworld\n".getBytes());
			fos.close();
			channels.input().send(new GenericMessage<File>(file));
			Message<?> m = collector.forChannel(channels.output()).poll(10, TimeUnit.SECONDS);
			assertNotNull(m);
			assertNull((m.getHeaders().get(IntegrationMessageHeaderAccessor.SEQUENCE_NUMBER)));
			assertThat(m, hasPayload("hello"));
			assertThat(collector.forChannel(channels.output()), receivesPayloadThat(is("world")));
			file.delete();
		}
	}

	@IntegrationTest({ "fileMarkers = true", "charset = UTF-8" })
	public static class fromFileWithMarkersTests extends SplitterProcessorIntegrationTests {

		@Test
		public void test() throws Exception {
			assertThat(this.splitter, instanceOf(FileSplitter.class));
			assertSame(this.splitter, TestUtils.getPropertyValue(this.consumer, "handler"));
			File file = new File(System.getProperty("java.io.tmpdir") + File.separator + "splitter.proc.test");
			FileOutputStream fos = new FileOutputStream(file);
			fos.write("hello\nworld\n".getBytes());
			fos.close();
			channels.input().send(new GenericMessage<File>(file));
			Message<?> m = collector.forChannel(channels.output()).poll(10, TimeUnit.SECONDS);
			assertNotNull(m);
			assertThat(m.getPayload(), instanceOf(FileMarker.class));
			assertThat((FileMarker) m.getPayload(), hasMark(Mark.START));
			assertThat(m, allOf(hasSequenceNumber(1), hasSequenceSize(0)));
			assertThat(collector.forChannel(channels.output()), receivesPayloadThat(is("hello")));
			assertThat(collector.forChannel(channels.output()), receivesPayloadThat(is("world")));
			assertThat(collector.forChannel(channels.output()),
					receivesPayloadThat(allOf(
							instanceOf(FileMarker.class),
				 			hasMark(Mark.END))));
			file.delete();
		}
	}

	@IntegrationTest("expression=payload.split(',')")
	public static class UsingExpressionIntegrationTests extends SplitterProcessorIntegrationTests {

		@Test
		public void test() {
			assertThat(this.splitter, instanceOf(ExpressionEvaluatingSplitter.class));
			channels.input().send(new GenericMessage<Object>("hello,world"));
			assertThat(collector.forChannel(channels.output()), receivesPayloadThat(is("hello")));
			assertThat(collector.forChannel(channels.output()), receivesPayloadThat(is("world")));
		}
	}

	private static MarkerMatcher hasMark(Mark expected) {
		return new MarkerMatcher(expected);
	}

	private static final class MarkerMatcher extends DiagnosingMatcher<FileMarker> {

		private final Mark expected;

		private MarkerMatcher(Mark expected) {
			this.expected = expected;
		}

		@Override
		public void describeTo(Description description) {
			description.appendText("the mark to be ").appendText(this.expected.name());
		}

		@Override
		protected boolean matches(Object item, Description mismatchDescription) {
			Mark mark = ((FileMarker) item).getMark();
			boolean match = this.expected.compareTo(mark) == 0;
			if (!match) {
				mismatchDescription.appendText("is ").appendText(mark.name());
			}
			return match;
		}

	}

}
