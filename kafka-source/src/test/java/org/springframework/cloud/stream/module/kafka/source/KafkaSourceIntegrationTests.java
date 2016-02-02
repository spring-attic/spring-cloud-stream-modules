/*
 *
 *  * Copyright 2016 the original author or authors.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.springframework.cloud.stream.module.kafka.source;

import kafka.admin.AdminUtils;
import kafka.server.KafkaServer;
import kafka.utils.TestUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.cloud.stream.modules.test.PropertiesInitializer;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.cloud.stream.test.junit.kafka.KafkaTestSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.kafka.support.KafkaHeaders;
import org.springframework.integration.kafka.util.TopicUtils;
import org.springframework.messaging.Message;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static scala.collection.JavaConversions.asScalaBuffer;

/**
 * Tests for basic {@link KafkaSource}.
 *
 * @author Soby Chacko
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {KafkaSourceIntegrationTests.ContextConfiguration.class, KafkaSourceApplication.class},
		initializers = PropertiesInitializer.class)
@DirtiesContext
public abstract class KafkaSourceIntegrationTests {

	private static String scs_kafka_test_embedded;

	private static final Map<String, Integer> TEST_TOPICS_WITH_PARTITIONS = new HashMap<>();

	private static final String TEST_TOPIC1 = "foo";
	private static final String TEST_TOPIC2 = "bar";

	static {
		scs_kafka_test_embedded = System.getProperty("SCS_KAFKA_TEST_EMBEDDED");
		System.setProperty("SCS_KAFKA_TEST_EMBEDDED", "true");

		TEST_TOPICS_WITH_PARTITIONS.put(TEST_TOPIC1, 1);
		TEST_TOPICS_WITH_PARTITIONS.put(TEST_TOPIC2, 4);
	}

	@ClassRule
	public static KafkaTestSupport kafkaTestSupport = new KafkaTestSupport();

	@Autowired
	protected MessageCollector messageCollector;

	@Autowired
	protected Source source;

	@Autowired
	protected Producer<String, String> producer;

	@BeforeClass
	public static void configureZkProps() throws Throwable {
		Assert.assertTrue("Embedded kafka instance must be setup by now...", kafkaTestSupport != null);
		Properties properties = new Properties();
		properties.put("zkConnect", kafkaTestSupport.getZkConnectString());
		PropertiesInitializer.PROPERTIES = properties;
	}

	@AfterClass
	public static void cleanup() {
		if (scs_kafka_test_embedded != null) {
			System.setProperty("SCS_KAFKA_TEST_EMBEDDED", scs_kafka_test_embedded);
		} else {
			System.clearProperty("SCS_KAFKA_TEST_EMBEDDED");
		}
	}

	@IntegrationTest({"topics = foo"})
	public static class BasicTopicTest extends KafkaSourceIntegrationTests {

		@Test
		public void test() throws Exception {

			for (int i = 0; i < 10; i++) {
				producer.send(new ProducerRecord<>(TEST_TOPIC1,
						"test-topic" + i,
						Integer.toString(i)));
			}

			for (int i = 0; i < 10; i++) {
				Message<?> out = this.messageCollector.forChannel(this.source.output()).poll(10, TimeUnit.SECONDS);
				assertNotNull(out);
				String payload = new String(((byte[]) out.getPayload()), "UTF-8");
				assertEquals(Integer.toString(i), payload);
				assertEquals(TEST_TOPIC1, out.getHeaders().get(KafkaHeaders.TOPIC));
			}

		}
	}

	@IntegrationTest({"partitions.bar=0,1,2,3"})
	public static class TopicAllPartitionTest extends KafkaSourceIntegrationTests {

		@Test
		public void testTopicWithAllAvailablePartitions() throws Exception {
			//10 messages going in, should consume all 10
			for (int i = 0; i < 10; i++) {
				producer.send(new ProducerRecord<>(TEST_TOPIC2, i % 4,
						"test-topic" + i,
						Integer.toString(i)));
			}

			List<String> strings = new ArrayList<>();
			for (int i = 0; i < 10; i++) {
				Message<?> out = this.messageCollector.forChannel(this.source.output()).poll(10, TimeUnit.SECONDS);
				assertNotNull(out);
				String payload = new String(((byte[]) out.getPayload()), "UTF-8");
				strings.add(payload);
				assertEquals(TEST_TOPIC2, out.getHeaders().get(KafkaHeaders.TOPIC));
			}

			//Verify that we read from all the partitions and all messages are accounted for
			for (int i = 0; i < 10; i++) {
				assertTrue(strings.contains(Integer.toString(i)));
			}
		}
	}

	@IntegrationTest({"partitions.bar=0,1,2"})
	public static class TopicSelectedPartitionsTest extends KafkaSourceIntegrationTests {

		@Test
		public void testTopicWithOnlyConfiguredPartitions() throws Exception {
			//12 messages going in to 4 partitions, but only 9 should be consumed as we don't consume from partition 3.
			for (int i = 0; i < 12; i++) {
				producer.send(new ProducerRecord<>(TEST_TOPIC2, i % 4,
						"test-topic" + i,
						Integer.toString(i)));
			}

			List<String> strings = new ArrayList<>();
			for (int i = 0; i < 9; i++) {
				Message<?> out = this.messageCollector.forChannel(this.source.output()).poll(10, TimeUnit.SECONDS);
				assertNotNull(out);
				String payload = new String(((byte[]) out.getPayload()), "UTF-8");
				strings.add(payload);
				assertEquals(TEST_TOPIC2, out.getHeaders().get(KafkaHeaders.TOPIC));
			}

			Message<?> out = this.messageCollector.forChannel(this.source.output()).poll(4, TimeUnit.SECONDS);
			assertNull(out);

			//Verify that we read from all the partitions and all messages are accounted for
			Integer[] in = new Integer[]{0, 1, 2, 4, 5, 6, 8, 9, 10};
			for (int i : in) {
				assertTrue(strings.contains(Integer.toString(i)));
			}
			Integer[] excluded = new Integer[]{3, 7, 11};
			for (int i : excluded) {
				assertFalse(strings.contains(Integer.toString(i)));
			}
		}
	}

	@IntegrationTest({"partitions.bar=0,1,2,3", "initialOffsets.bar.0=5"})
	public static class InitialOffsetForOnePartitionStartsOutsideRangeTest extends KafkaSourceIntegrationTests {

		@Test
		public void testTopicWithOnlyConfiguredPartitions() throws Exception {

			//12 messages produced, only 9 are consumed as partition 0's initial offsets are outside
			//range and not committed to zookeeper.
			for (int i = 0; i < 12; i++) {
				producer.send(new ProducerRecord<>(TEST_TOPIC2, i % 4,
						"test-topic" + i,
						Integer.toString(i)));
			}

			List<String> strings = new ArrayList<>();
			for (int i = 0; i < 9; i++) {
				Message<?> out = this.messageCollector.forChannel(this.source.output()).poll(10, TimeUnit.SECONDS);
				assertNotNull(out);
				String payload = new String(((byte[]) out.getPayload()), "UTF-8");
				strings.add(payload);
				assertEquals(TEST_TOPIC2, out.getHeaders().get(KafkaHeaders.TOPIC));
			}

			Message<?> out = this.messageCollector.forChannel(this.source.output()).poll(4, TimeUnit.SECONDS);
			assertNull(out);

			//Verify that we read from all the partitions and all messages are accounted for
			Integer[] in = new Integer[]{1, 2, 3, 5, 6, 7, 9, 10, 11};
			for (int i : in) {
				assertTrue(strings.contains(Integer.toString(i)));
			}
			Integer[] excluded = new Integer[]{0, 4, 8};
			for (int i : excluded) {
				assertFalse(strings.contains(Integer.toString(i)));
			}
		}
	}

	@IntegrationTest({"partitions.bar=0,1,2,3", "initialOffsets.bar.0=0"})
	public static class InitialOffsetForOnePartitionSetExplicitlyToBeginningTest extends KafkaSourceIntegrationTests {

		@Test
		public void testTopicWithOnlyConfiguredPartitions() throws Exception {

			//12 messages are going in, all 12 should be consumed from 4 partitions.
			for (int i = 0; i < 12; i++) {
				producer.send(new ProducerRecord<>(TEST_TOPIC2, i % 4,
						"test-topic" + i,
						Integer.toString(i)));
			}

			List<String> strings = new ArrayList<>();
			for (int i = 0; i < 12; i++) {
				Message<?> out = this.messageCollector.forChannel(this.source.output()).poll(10, TimeUnit.SECONDS);
				assertNotNull(out);
				String payload = new String(((byte[]) out.getPayload()), "UTF-8");
				strings.add(payload);
				assertEquals(TEST_TOPIC2, out.getHeaders().get(KafkaHeaders.TOPIC));
			}

			//Verify that we read from all the partitions and all messages are accounted for
			Integer[] in = new Integer[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
			for (int i : in) {
				assertTrue(strings.contains(Integer.toString(i)));
			}
		}
	}

	@Configuration
	@EnableIntegration
	public static class ContextConfiguration {

		@Bean
		public Producer<String, String> producer() {

			Properties props = new Properties();
			props.put("queue.buffering.max.ms", "15000");
			props.put("bootstrap.servers", kafkaTestSupport.getBrokerAddress());
			props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
			props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
			Producer<String, String> producer = new KafkaProducer<>(props);

			return producer;
		}

		@Bean(destroyMethod = "destroy")
		public InitializingBean topicManager() {
			return new InitializingBean() {

				@Override
				public void afterPropertiesSet() throws Exception {
					for (Map.Entry<String, Integer> topicWithParts : TEST_TOPICS_WITH_PARTITIONS.entrySet()) {
						TopicUtils.ensureTopicCreated(kafkaTestSupport.getZkConnectString(), topicWithParts.getKey(),
								topicWithParts.getValue(), 1);
					}
				}

				public void destroy() {

					for (Map.Entry<String, Integer> topicWithParts : TEST_TOPICS_WITH_PARTITIONS.entrySet()) {
						AdminUtils.deleteTopic(kafkaTestSupport.getZkClient(), topicWithParts.getKey());
						List<KafkaServer> kafkaServers = new ArrayList<>();
						kafkaServers.add(kafkaTestSupport.getKafkaServer());
						TestUtils.waitUntilMetadataIsPropagated(asScalaBuffer(kafkaServers), topicWithParts.getKey(), 0, 5000L);
					}

					producer().close();
				}

			};
		}
	}
}
