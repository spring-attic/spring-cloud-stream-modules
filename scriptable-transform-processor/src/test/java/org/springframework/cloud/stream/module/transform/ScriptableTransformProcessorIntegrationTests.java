/*
 * Copyright 2015-2016 the original author or authors.
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
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Integration Tests for the Script Transform Processor.
 *
 * @author Andy Clement
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = ScriptableTransformProcessorApplication.class)
@WebIntegrationTest(randomPort = true)
@DirtiesContext
public abstract class ScriptableTransformProcessorIntegrationTests {

	@Autowired
	@Bindings(ScriptableTransformProcessor.class)
	protected Processor channels;

	@Autowired
	protected MessageCollector collector;

	@WebIntegrationTest({"script=function add(a,b) { return a+b;};add(1,3)", "language=js"})
	public static class JavascriptScriptProperty1Tests extends ScriptableTransformProcessorIntegrationTests {

		@Test
		public void testJavascriptFunctions() {
			channels.input().send(new GenericMessage<Object>("hello world"));
			assertThat(collector.forChannel(channels.output()), receivesPayloadThat(is(4L)));
		}
	}

	@WebIntegrationTest({"script=payload+foo", "language=js", "variables=foo=\\\\\40WORLD"})
	public static class JavascriptScriptProperty2Tests extends ScriptableTransformProcessorIntegrationTests {

		@Test
		public void testJavascriptSimple() {
			channels.input().send(new GenericMessage<Object>("hello world"));
			assertThat(collector.forChannel(channels.output()), receivesPayloadThat(is("hello world WORLD")));
		}

	}

	@WebIntegrationTest({"script=payload*limit", "language=js", "variables=limit=5"})
	public static class JavascriptScriptProperty3Tests extends ScriptableTransformProcessorIntegrationTests {

		@Test
		public void testJavascriptSimple() {
			channels.input().send(new GenericMessage<Object>(9));
			assertThat(collector.forChannel(channels.output()), receivesPayloadThat(is(45.0)));
		}

	}

	@WebIntegrationTest({"script=payload+foo", "language=groovy", "variables=foo=\\\\\40WORLD"})
	public static class GroovyScriptProperty1Tests extends ScriptableTransformProcessorIntegrationTests {

		@Test
		public void testGroovyBasic() {
			channels.input().send(new GenericMessage<Object>("hello world"));
			assertThat(collector.forChannel(channels.output()), receivesPayloadThat(is("hello world WORLD")));
		}

	}

	@WebIntegrationTest({"script=payload.substring(0, limit as int) + foo", "language=groovy",
			"variables=limit=5\\n foo=\\\\\40WORLD"})
	public static class GroovyScriptProperty2Tests extends ScriptableTransformProcessorIntegrationTests {

		@Test
		public void testGroovyComplex() {
			channels.input().send(new GenericMessage<Object>("hello world"));
			assertThat(collector.forChannel(channels.output()), receivesPayloadThat(is("hello WORLD")));
		}

	}

	@WebIntegrationTest({"script=return \"\"#{payload.upcase}\"\"", "language=ruby"})
	public static class RubyScriptProperty1Tests extends ScriptableTransformProcessorIntegrationTests {

		@Test
		public void testRubyScript() {
			channels.input().send(new GenericMessage<Object>("hello world"));
			assertThat(collector.forChannel(channels.output()), receivesPayloadThat(is("HELLO WORLD")));
		}

	}

	@WebIntegrationTest({"script=\"def foo(x)\\n  return x+5\\nend\\nfoo(payload)\\n\"", "language=ruby"})
	public static class RubyScriptProperty2Tests extends ScriptableTransformProcessorIntegrationTests {

		@Test
		public void testRuby() {
			channels.input().send(new GenericMessage<Object>(9));
			assertThat(collector.forChannel(channels.output()), receivesPayloadThat(is(14L)));
		}

	}

	// Python not currently supported, problems running it in SCDF

	@WebIntegrationTest({"script=\"def multiply(x,y):\\n  return x*y\\nanswer = multiply(payload,5)\\n\"",
			"language=python"})
	public static class PythonScriptProperty1Tests extends ScriptableTransformProcessorIntegrationTests {

		@Test
		public void testPython() {
			channels.input().send(new GenericMessage<Object>(6));
			assertThat(collector.forChannel(channels.output()), receivesPayloadThat(is(30)));
		}

	}

	@WebIntegrationTest({"script=\"def concat(x,y):\\n  return x+y\\nanswer = concat(\"\"hello \"\",payload)\\n\"",
			"language=python"})
	public static class PythonScriptProperty2Tests extends ScriptableTransformProcessorIntegrationTests {

		@Test
		public void testPython() {
			channels.input().send(new GenericMessage<Object>("world"));
			assertThat(collector.forChannel(channels.output()), receivesPayloadThat(is("hello world")));
		}

	}

}
