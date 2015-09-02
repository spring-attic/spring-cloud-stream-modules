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

package org.springframework.cloud.stream.module.time;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.cloud.stream.annotation.Bindings;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = TimeSourceApplication.class)
@WebIntegrationTest(randomPort = true)
@DirtiesContext
public abstract class TimeSourceIntegrationTests {

	@Autowired
	@Bindings(TimeSource.class)
	protected Source timeSource;

	@Autowired
	protected MessageCollector messageCollector;

	public static class DefaultPropertiesTests extends TimeSourceIntegrationTests {
		@Test
		public void test() throws InterruptedException {
			Thread.sleep(1100);
			assertEquals(2, messageCollector.forChannel(timeSource.output()).size());
		}
	}

	@IntegrationTest({"timeUnit=MILLISECONDS" })
	public static class TimeUnitPropertiesTests extends TimeSourceIntegrationTests {
		@Test
		public void test() throws InterruptedException {
			Thread.sleep(1000);
			assertThat(messageCollector.forChannel(timeSource.output()).size(), Matchers.greaterThanOrEqualTo(500));
		}
	}
}
