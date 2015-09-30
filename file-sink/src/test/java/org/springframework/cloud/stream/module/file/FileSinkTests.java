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

package org.springframework.cloud.stream.module.file;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileReader;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.FileCopyUtils;

/**
 * @author Mark Fisher
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = FileSinkApplication.class)
@WebIntegrationTest(randomPort = true)
@DirtiesContext
public abstract class FileSinkTests {

	@Autowired
	protected Sink sink;

	@WebIntegrationTest({"name = test", "dir = /tmp/dataflow-tests", "suffix=txt"})
	public static class TextTests extends FileSinkTests {

		@Test
		public void test() throws Exception {
			sink.input().send(MessageBuilder.withPayload("this is a test").build());
			File file = new File("/tmp/dataflow-tests/test.txt");
			file.deleteOnExit();
			assertTrue("file does not exist", file.exists());
			assertEquals("this is a test\n", FileCopyUtils.copyToString(new FileReader(file)));
		}
	}

	@WebIntegrationTest({"binary = true", "dir = /tmp/dataflow-tests"})
	public static class BinaryTests extends FileSinkTests {

		@Test
		public void test() throws Exception {
			sink.input().send(MessageBuilder.withPayload("foo".getBytes()).build());
			File file = new File("/tmp/dataflow-tests/file-sink");
			file.deleteOnExit();
			assertTrue("file does not exist", file.exists());
			byte[] results = FileCopyUtils.copyToByteArray(file);
			assertEquals(3, results.length);
			assertEquals((byte)'f', results[0]);
			assertEquals((byte)'o', results[1]);
			assertEquals((byte)'o', results[2]);
		}
	}

	@WebIntegrationTest({"nameExpression = payload.substring(0, 4)",
			"dirExpression = '/tmp/dataflow-tests/'+headers.dir", "suffix=out"})
	public static class ExpressionTests extends FileSinkTests {

		@Test
		public void test() throws Exception {
			sink.input().send(MessageBuilder.withPayload("this is another test")
					.setHeader("dir", "expression").build());
			File file = new File("/tmp/dataflow-tests/expression/this.out");
			file.deleteOnExit();
			assertTrue("file does not exist", file.exists());
			assertEquals("this is another test\n", FileCopyUtils.copyToString(new FileReader(file)));
		}
	}
}
