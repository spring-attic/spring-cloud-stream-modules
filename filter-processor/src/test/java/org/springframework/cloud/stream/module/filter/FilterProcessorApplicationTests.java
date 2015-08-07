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

package org.springframework.cloud.stream.module.filter;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.ModuleChannels;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.cloud.stream.annotation.Processor;
import org.springframework.cloud.stream.annotation.Sink;
import org.springframework.cloud.stream.annotation.Source;
import org.springframework.messaging.MessageChannel;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = FilterProcessorApplication.class)
@WebAppConfiguration
@DirtiesContext
public class FilterProcessorApplicationTests {

	@Autowired
	@ModuleChannels(FilterProcessor.class)
	private Processor processor;

	@Autowired
	private Processor same;

	@Output(Source.OUTPUT)
	private MessageChannel output;

	@Input(Sink.INPUT)
	private MessageChannel input;

	@Test
	@Ignore
	public void contextLoads() {
		assertNotNull(this.processor.input());
		assertNotNull(this.processor.output());
		assertSame(this.processor.input(), same.input());
		assertSame(this.processor.output(), same.output());
	}
}
