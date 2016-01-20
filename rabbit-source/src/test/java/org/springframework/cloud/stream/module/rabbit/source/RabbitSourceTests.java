/*
 * Copyright 2016 the original author or authors.
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

package org.springframework.cloud.stream.module.rabbit.source;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;

import org.aopalliance.aop.Advice;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.cloud.stream.test.junit.rabbit.RabbitTestSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.test.util.TestUtils;
import org.springframework.messaging.Message;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Tests for RabbitSource.
 *
 * @author Gary Russell
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { RabbitSourceApplication.class, RabbitSourceTests.Config.class })
@DirtiesContext
public abstract class RabbitSourceTests {

	@Rule
	public RabbitTestSupport rabbitAvailable;

	@Autowired
	protected Source channels;

	@Autowired
	protected MessageCollector messageCollector;

	@Autowired
	protected RabbitSourceProperties properties;

	@Autowired
	protected SimpleMessageListenerContainer container;

	@Autowired
	protected RabbitTemplate rabbitTemplate;

	@IntegrationTest({ "queues = scsm-testq", "enableRetry = true",
		"initialRetryInterval = 123", "maxRetryInterval = 345", "retryMultiplier = 1.5", "maxAttempts = 5",
		"requeue = false",
		"mappedRequestHeaders = STANDARD_REQUEST_HEADERS,bar",
		"spring.rabbitmq.listener.concurrency = 2", "spring.rabbitmq.listener.maxConcurrency = 3 ",
		"spring.rabbitmq.listener.acknowledgeMode = NONE", "spring.rabbitmq.listener.prefetch = 10",
		"spring.rabbitmq.listener.transactionSize = 5" })
	public static class PropertiesPopulatedTests extends RabbitSourceTests {

		@Test
		public void test() throws Exception {
			Advice[] adviceChain = TestUtils.getPropertyValue(this.container, "adviceChain", Advice[].class);
			assertEquals(1, adviceChain.length);
			RetryTemplate retryTemplate = TestUtils.getPropertyValue(adviceChain[0], "retryOperations",
					RetryTemplate.class);
			assertEquals(5, TestUtils.getPropertyValue(retryTemplate, "retryPolicy.maxAttempts"));
			assertEquals(123L, TestUtils.getPropertyValue(retryTemplate, "backOffPolicy.initialInterval"));
			assertEquals(345L, TestUtils.getPropertyValue(retryTemplate, "backOffPolicy.maxInterval"));
			assertEquals(1.5, TestUtils.getPropertyValue(retryTemplate, "backOffPolicy.multiplier"));
			assertEquals("scsm-testq", this.container.getQueueNames()[0]);
			assertFalse(TestUtils.getPropertyValue(this.container, "defaultRequeueRejected", Boolean.class));
			assertEquals(2, TestUtils.getPropertyValue(this.container, "concurrentConsumers"));
			assertEquals(3, TestUtils.getPropertyValue(this.container, "maxConcurrentConsumers"));
			assertEquals(AcknowledgeMode.NONE, TestUtils.getPropertyValue(this.container, "acknowledgeMode"));
			assertEquals(10, TestUtils.getPropertyValue(this.container, "prefetchCount"));
			assertEquals(5, TestUtils.getPropertyValue(this.container, "txSize"));

			this.rabbitTemplate.convertAndSend("", "scsm-testq", "foo", new MessagePostProcessor() {

				@Override
				public org.springframework.amqp.core.Message postProcessMessage(
						org.springframework.amqp.core.Message message) throws AmqpException {
					message.getMessageProperties().getHeaders().put("bar", "baz");
					return message;
				}

			});
			Message<?> out = this.messageCollector.forChannel(this.channels.output()).poll(10,  TimeUnit.SECONDS);
			assertNotNull(out);
			assertEquals("foo", out.getPayload());
			assertEquals("baz", out.getHeaders().get("bar"));
		}

	}

	@IntegrationTest({ "queues = scsm-testq,scsm-testq2", "enableRetry = false",
		"transacted = true",
		"spring.rabbitmq.listener.acknowledgeMode = AUTO" })
	public static class NoRetryAndTxTests extends RabbitSourceTests {

		@Test
		public void test() throws Exception {
			Advice[] adviceChain = TestUtils.getPropertyValue(this.container, "adviceChain", Advice[].class);
			assertEquals(0, adviceChain.length);
			assertTrue(TestUtils.getPropertyValue(this.container, "transactional", Boolean.class));
			assertEquals(AcknowledgeMode.AUTO, TestUtils.getPropertyValue(this.container, "acknowledgeMode"));
			assertEquals("scsm-testq", this.container.getQueueNames()[0]);
			assertEquals("scsm-testq2", this.container.getQueueNames()[1]);
		}

	}

	@Configuration
	static class Config {

		@Bean
		public Queue queue() {
			return new Queue("scsm-testq", false, false, true);
		}

		@Bean
		public Queue queue2() {
			return new Queue("scsm-testq2", false, false, true);
		}

	}

}
