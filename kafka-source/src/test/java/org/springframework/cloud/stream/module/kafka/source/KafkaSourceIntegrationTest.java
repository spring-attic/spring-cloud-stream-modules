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

import com.gs.collections.api.block.function.Function0;
import com.gs.collections.impl.list.mutable.FastList;
import kafka.admin.AdminUtils;
import kafka.serializer.Encoder;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.cloud.stream.test.junit.kafka.KafkaTestSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.IntegrationMessageHeaderAccessor;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.GenericEndpointSpec;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlowDefinition;
import org.springframework.integration.dsl.PublishSubscribeSpec;
import org.springframework.integration.dsl.kafka.Kafka;
import org.springframework.integration.dsl.kafka.KafkaProducerMessageHandlerSpec;
import org.springframework.integration.dsl.support.Consumer;
import org.springframework.integration.dsl.support.Function;
import org.springframework.integration.dsl.support.PropertiesBuilder;
import org.springframework.integration.expression.ValueExpression;
import org.springframework.integration.kafka.outbound.KafkaProducerMessageHandler;
import org.springframework.integration.kafka.serializer.common.StringEncoder;
import org.springframework.integration.kafka.support.KafkaHeaders;
import org.springframework.integration.kafka.support.ProducerMetadata;
import org.springframework.integration.kafka.util.EncoderAdaptingSerializer;
import org.springframework.integration.kafka.util.TopicUtils;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.GenericMessage;
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
    @Qualifier("sendToKafkaFlow.input")
    protected MessageChannel sendToKafkaFlowInput;

    @Autowired
    @Qualifier("kafkaProducer.handler")
    protected KafkaProducerMessageHandler kafkaProducer;

    @Autowired
    protected Source source;

    @Test
    public void testBasicSource() throws Exception {
        this.kafkaProducer.setPartitionIdExpression(new ValueExpression<>(0));

        this.sendToKafkaFlowInput.send(new GenericMessage<>("foo"));

        for (int i = 0; i < 100; i++) {
            Message<?> out = this.messageCollector.forChannel(this.source.output()).poll(10, TimeUnit.SECONDS);
            assertNotNull(out);
            String payload = new String(((byte[]) out.getPayload()), "UTF-8");
            assertEquals("foo", payload);
            assertEquals(TEST_TOPIC, out.getHeaders().get(KafkaHeaders.TOPIC));
        }
    }

    @Configuration
    @EnableIntegration
    public static class ContextConfiguration {

        @Bean
        public IntegrationFlow sendToKafkaFlow() {
            return new IntegrationFlow() {
                @Override
                public void configure(IntegrationFlowDefinition<?> f) {
                    f.<String>split(new Function<Object, Object>() {
                        @Override
                        public Object apply(final Object p) {
                            return FastList.newWithNValues(100, new Function0<Object>() {
                                @Override
                                public Object value() {
                                    return p;
                                }
                            });
                        }
                    }, null)
                    .publishSubscribeChannel(new Consumer<PublishSubscribeSpec>() {
                         @Override
                         public void accept(PublishSubscribeSpec c) {
                             c.subscribe(new IntegrationFlow() {
                                 @Override
                                 public void configure(IntegrationFlowDefinition<?> sf) {
                                     sf.handle(kafkaMessageHandler(kafkaTestSupport.getBrokerAddress(), TEST_TOPIC),
                                         new Consumer<GenericEndpointSpec<KafkaProducerMessageHandler>>() {
                                             @Override
                                             public void accept(GenericEndpointSpec<KafkaProducerMessageHandler> e) {
                                                 e.id("kafkaProducer");
                                             }
                                         }
                                     );
                                 }
                             });
                         }
                    });
                }
            };
        }
    }

    @SuppressWarnings("unchecked")
    private static KafkaProducerMessageHandlerSpec kafkaMessageHandler(String serverAddress, String topic) {
        Encoder<?> stringEncoder = new StringEncoder();

        Properties props = new Properties();
        props.put("queue.buffering.max.ms", "15000");

        return Kafka
                .outboundChannelAdapter(new Consumer<PropertiesBuilder>() {
                    @Override
                    public void accept(PropertiesBuilder props) {
                        props
                                .put("queue.buffering.max.ms", "15000");
                    }
                })
                .messageKey(new Function<Message<Object>, Object>() {
                    @Override
                    public Object apply(Message<Object> m) {
                        return "test-topic" + m
                                .getHeaders()
                                .get(IntegrationMessageHeaderAccessor.SEQUENCE_NUMBER);
                    }
                })
                .addProducer(
                        new ProducerMetadata<>(topic, String.class, String.class,
                                new EncoderAdaptingSerializer<>((Encoder<String>) stringEncoder),
                                new StringSerializer()),
                        serverAddress);
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
