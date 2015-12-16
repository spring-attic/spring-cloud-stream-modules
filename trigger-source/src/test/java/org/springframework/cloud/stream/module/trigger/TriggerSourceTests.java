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

package org.springframework.cloud.stream.module.trigger;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.cloud.stream.annotation.Bindings;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Trigger source tests.
 * @author Ilayaperumal Gopinathan
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = TriggerSourceApplication.class)
@WebIntegrationTest(randomPort = true)
@DirtiesContext
public abstract class TriggerSourceTests {

	@Autowired
	@Bindings(TriggerSource.class)
	protected Source triggerSource;

	@Autowired
	protected MessageCollector messageCollector;

	@WebIntegrationTest({"fixedDelay=2", "initialDelay=1", "payload=test"})
	public static class FixedDelayTest extends TriggerSourceTests {

		@Test
		public void fixedDelayTest() throws InterruptedException {
			Thread.sleep(2100);
			assertEquals(1, messageCollector.forChannel(triggerSource.output()).size());
			messageCollector.forChannel(triggerSource.output()).take().getPayload().equals("test");
		}
	}

	@WebIntegrationTest({"cron=0/2 * * * * *", "payload=cronTest"})
	public static class CronTriggerTest extends TriggerSourceTests {

		@Test
		public void cronTriggerTest() throws InterruptedException {
			Thread.sleep(4100);
			assertEquals(2, messageCollector.forChannel(triggerSource.output()).size());
			messageCollector.forChannel(triggerSource.output()).take().getPayload().equals("cronTest");
			messageCollector.forChannel(triggerSource.output()).take().getPayload().equals("cronTest");
		}
	}
}
