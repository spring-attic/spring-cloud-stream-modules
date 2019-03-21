/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.cloud.stream.module.loggregator.source;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudfoundry.client.lib.CloudFoundryClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.cloud.stream.module.loggregator.source.LoggregatorProperties;
import org.springframework.cloud.stream.module.loggregator.source.LoggregatorSourceApplication;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Test the Spring Cloud Dataflow CloudFoundry Loggregator source
 *
 * @author Josh Long
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = LoggregatorSourceApplication.class)
@EnableConfigurationProperties(LoggregatorProperties.class)
@IntegrationTest({"applicationName=${cf.app}", "cloudFoundryUser=${cf.user}",
		"cloudFoundryPassword=${cf.password}", "cloudFoundryApi=${cf.api}"})
@DirtiesContext
public class LoggregatorSourceTest {

	private Log log = LogFactory.getLog(getClass());

	@Autowired
	private Source channels;

	@Autowired
	private MessageCollector messageCollector;

	@Autowired
	private CloudFoundryClient cloudFoundryClient;

	@Autowired
	private LoggregatorProperties loggregatorProperties;

	private RestTemplate restTemplate = new RestTemplate();

	@Test
	public void testLogReceipt() throws Exception {

		log.info(String.format("testing application %s against CF API endpoint %s with CF user %s",
				this.loggregatorProperties.getApplicationName(),
				this.loggregatorProperties.getCloudFoundryApi(),
				this.loggregatorProperties.getCloudFoundryUser()));

		String traceMessage = "logged-message-" + System.currentTimeMillis();
		ResponseEntity<String> entity = this.restTemplate.getForEntity(
				this.urlForApplication() + "/{uri}", String.class, Collections.singletonMap("uri", traceMessage));
		assertEquals(entity.getStatusCode(), HttpStatus.OK);
		assertEquals(entity.getBody(), traceMessage);

		BlockingQueue<Message<?>> messageBlockingQueue = this.messageCollector.forChannel(this.channels.output());

		Message<?> message;
		int count = 0, max = 20;

		while ((message = messageBlockingQueue.poll(1, TimeUnit.MINUTES)) != null && (count++ < max)) { // this could run for for 20 minutes
			log.info(String.format("received the following log from Loggregator: %s", message.getPayload()));
			String payload = String.class.cast(message.getPayload());
			assertNotNull(payload, "the message can't be null!");
			if (payload.contains(traceMessage)) {
				log.info("---------------------------------------------");
				log.info(String.format("loggregator tracer message: %s", traceMessage));
				log.info(String.format("delivered: %s", payload));
				log.info("---------------------------------------------");
				return;
			}
		}
		fail("tracer message was never delivered");
	}

	private String urlForApplication() {
		String uri = "http://" + this.cloudFoundryClient.getApplication(this.loggregatorProperties.getApplicationName())
				.getUris()
				.iterator()
				.next();
		this.log.info("uri of the application to test is " + uri);
		return uri;
	}

}