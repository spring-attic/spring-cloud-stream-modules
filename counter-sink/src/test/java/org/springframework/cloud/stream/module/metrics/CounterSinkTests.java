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

package org.springframework.cloud.stream.module.metrics;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.messaging.support.GenericMessage;

/**
 * Unit tests for CounterSink.
 *
 * @author Eric Bottard
 */
@RunWith(MockitoJUnitRunner.class)
public class CounterSinkTests {

	private CounterSink counterSink;

	@Mock
	private CounterService counterService;

	private CounterSinkOptions options;

	@Before
	public void setUp() throws Exception {
		counterSink = new CounterSink();
		options = new CounterSinkOptions();
		counterSink.setOptions(options);

		counterSink.setCounterService(counterService);
	}

	@Test
	public void usingSimpleCounterName() {

		options.setName("foobar");

		counterSink.counterSink(new GenericMessage<Object>("hello"));

		Mockito.verify(counterService).increment("foobar");
	}

	@Test
	public void usingComputedCounterName() {
		options.setNameExpression("payload.substring(0, 4)");
		counterSink.setOptions(options);
		counterSink.setCounterService(counterService);

		counterSink.counterSink(new GenericMessage<Object>("hello"));

		Mockito.verify(counterService).increment("hell");

	}

}