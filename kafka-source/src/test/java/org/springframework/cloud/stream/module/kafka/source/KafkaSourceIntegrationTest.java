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
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.cloud.stream.messaging.Source;
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

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests for {@link KafkaSource}.
 *
 * @author Soby Chacko
 */
@RunWith(SpringJUnit4ClassRunner.class)
@IntegrationTest({"topics = test-topic"})
@SpringApplicationConfiguration(classes = {KafkaSourceIntegrationTest.ContextConfiguration.class, KafkaSourceApplication.class})
@DirtiesContext
public class KafkaSourceIntegrationTest {

    private static final String TEST_TOPIC = "test-topic";

    @ClassRule
    public static KafkaTestSupport kafkaTestSupport = new KafkaTestSupport();

    @Autowired
    protected MessageCollector messageCollector;

    @Autowired
    protected Source source;

    @Autowired
    protected Producer<String, String> producer;

    @Test
    public void testBasicSource() throws Exception {

        for (int i = 0; i < 100; i++) {

            producer.send(new ProducerRecord<>("test-topic",
                    "test-topic" + i,
                    Integer.toString(i)));
        }
        producer.close();

        for (int i = 0; i < 100; i++) {
            Message<?> out = this.messageCollector.forChannel(this.source.output()).poll(10, TimeUnit.SECONDS);
            assertNotNull(out);
            String payload = new String(((byte[]) out.getPayload()), "UTF-8");
            assertEquals(Integer.toString(i), payload);
            assertEquals(TEST_TOPIC, out.getHeaders().get(KafkaHeaders.TOPIC));
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

                private final List<String> topics = Arrays.asList(TEST_TOPIC);

                @Override
                public void afterPropertiesSet() throws Exception {
                    this.topics.forEach(new java.util.function.Consumer<String>() {
                        @Override
                        public void accept(String t) {
                            TopicUtils.ensureTopicCreated(kafkaTestSupport.getZkConnectString(), t, 1, 1);
                        }
                    });
                }

                public void destroy() {
                    this.topics.forEach(new java.util.function.Consumer<String>() {
                        @Override
                        public void accept(String t) {
                            AdminUtils.deleteTopic(kafkaTestSupport.getZkClient(), t);
                            //TestUtils.waitUntilMetadataIsPropagated(asScalaBuffer(), t, 0, 5000L);
                        }
                    });
                }

            };
        }
    }
}
