/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.stream.module.dataset.sink;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.cloud.stream.annotation.Bindings;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.cloud.stream.module.dataset.domain.TestPojo;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.hadoop.fs.FsShell;
import org.springframework.data.hadoop.store.dataset.DatasetOperations;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;

/**
 * @author Thomas Risberg
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = DatasetSinkApplication.class)
@DirtiesContext
public abstract class DatasetSinkIntegrationTests {

	@Autowired
	ConfigurableApplicationContext applicationContext;

	@Value("${directory}#{T(java.io.File).separator}${namespace}")
	protected String testDir;

	@Autowired
	protected FsShell fsShell;

	@Autowired
	protected DatasetOperations datasetOperations;

	@Autowired
	@Bindings(DatasetSink.class)
	protected Sink sink;

	@Before
	public void setup() {
		if (fsShell.test(testDir)) {
			fsShell.rmr(testDir);
		}
	}

	@WebIntegrationTest({"server.port:0",
			"spring.hadoop.fsUri=file:///",
			"directory=${java.io.tmpdir}/dataset",
			"namespace=test",
			"batchSize=2",
			"idleTimeout=2000"})
	public static class WritingDatasetTests extends DatasetSinkIntegrationTests {

		@Test
		public void testWritingSomething() throws IOException {
			sink.input().send(new GenericMessage<>("Foo"));
			sink.input().send(new GenericMessage<>("Bar"));
			sink.input().send(new GenericMessage<>("Baz"));
		}

		@After
		public void checkFilesClosedOK() throws IOException {
			applicationContext.close();
			File testOutput = new File(testDir);
			assertTrue("Dataset path created", testOutput.exists());
			assertTrue("Dataset storage created",
					new File(testDir + File.separator + datasetOperations.getDatasetName(String.class)).exists());
			assertTrue("Dataset metadata created",
					new File(testDir + File.separator + datasetOperations.getDatasetName(String.class) +
							File.separator + ".metadata").exists());
			File testDatasetFiles =
					new File(testDir + File.separator + datasetOperations.getDatasetName(String.class));
			File[] files = testDatasetFiles.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					if (name.endsWith(".avro")) {
						return true;
					}
					return false;
				}
			});
			assertTrue("Dataset data files created", files.length > 1);
		}
	}

	@WebIntegrationTest({"server.port:0",
			"spring.hadoop.fsUri=file:///",
			"directory=${java.io.tmpdir}/dataset",
			"namespace=pojo",
			"allowNullValues=false",
			"format=avro",
			"partitionPath=year('timestamp')",
			"batchSize=2",
			"idleTimeout=2000"})
	public static class PartitionedDatasetTests extends DatasetSinkIntegrationTests {

		Long systemTime = 1446570266690L; // Date is 2015/11/03

		@Test
		public void testWritingSomething() throws IOException {
			TestPojo t1 = new TestPojo();
			t1.setId(1);
			t1.setTimestamp(systemTime + 10000);
			t1.setDescription("foo");
			sink.input().send(MessageBuilder.withPayload(t1).build());
			TestPojo t2 = new TestPojo();
			t2.setId(2);
			t2.setTimestamp(systemTime + 50000);
			t2.setDescription("x");
			sink.input().send(MessageBuilder.withPayload(t2).build());
		}

		@After
		public void checkFilesClosedOK() throws IOException {
			applicationContext.close();
			File testOutput = new File(testDir);
			assertTrue("Dataset path created", testOutput.exists());
			assertTrue("Dataset storage created",
					new File(testDir + File.separator + datasetOperations.getDatasetName(TestPojo.class)).exists());
			assertTrue("Dataset metadata created",
					new File(testDir + File.separator + datasetOperations.getDatasetName(TestPojo.class) +
							File.separator + ".metadata").exists());
			String year = new SimpleDateFormat("yyyy").format(systemTime);
			assertTrue("Dataset partition path created",
					new File(testDir + File.separator +
							datasetOperations.getDatasetName(TestPojo.class) + "/year=" + year).exists());
			File testDatasetFiles =
					new File(testDir + File.separator +
							datasetOperations.getDatasetName(TestPojo.class) + "/year=" + year);
			File[] files = testDatasetFiles.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					if (name.endsWith(".avro")) {
						return true;
					}
					return false;
				}
			});
			assertTrue("Dataset data files created", files.length > 0);
		}
	}

	@WebIntegrationTest({"server.port:0",
			"spring.hadoop.fsUri=file:///",
			"directory=${java.io.tmpdir}/dataset",
			"namespace=parquet",
			"allowNullValues=false",
			"format=parquet",
			"batchSize=2",
			"idleTimeout=2000"})
	public static class ParquetDatasetTests extends DatasetSinkIntegrationTests {

		@Test
		public void testWritingSomething() throws IOException {
			TestPojo t1 = new TestPojo();
			t1.setId(1);
			t1.setTimestamp(System.currentTimeMillis());
			t1.setDescription("foo");
			sink.input().send(MessageBuilder.withPayload(t1).build());
			TestPojo t2 = new TestPojo();
			t2.setId(2);
			t2.setTimestamp(System.currentTimeMillis());
			t2.setDescription("x");
			sink.input().send(MessageBuilder.withPayload(t2).build());
		}

		@After
		public void checkFilesClosedOK() throws IOException {
			applicationContext.close();
			File testOutput = new File(testDir);
			assertTrue("Dataset path created", testOutput.exists());
			assertTrue("Dataset storage created",
					new File(testDir + File.separator + datasetOperations.getDatasetName(TestPojo.class)).exists());
			assertTrue("Dataset metadata created",
					new File(testDir + File.separator + datasetOperations.getDatasetName(TestPojo.class) +
							File.separator + ".metadata").exists());
			File testDatasetFiles =
					new File(testDir + File.separator + datasetOperations.getDatasetName(TestPojo.class));
			File[] files = testDatasetFiles.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					if (name.endsWith(".parquet")) {
						return true;
					}
					return false;
				}
			});
			assertTrue("Dataset data files created", files.length > 0);
		}
	}
}
