/*
 * Copyright 2015 the original author or authors.
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

package ${package};

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.springframework.cloud.stream.test.matcher.MessageQueueMatcher.*;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.cloud.stream.annotation.ModuleChannels;
import org.springframework.cloud.stream.annotation.${typeAsInSourceProcessorOrSink};
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Tests for ${capitalizedNameOfModuleWithoutType}XXX${typeAsInSourceProcessorOrSink}.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = ${capitalizedNameOfModuleWithoutType}XXX${typeAsInSourceProcessorOrSink}Application.class)
@WebIntegrationTest(randomPort = true)
public abstract class ${capitalizedNameOfModuleWithoutType}XXX${typeAsInSourceProcessorOrSink}IntegrationTests {

	@Autowired
	@ModuleChannels(${capitalizedNameOfModuleWithoutType}XXX${typeAsInSourceProcessorOrSink}.class)
	protected ${typeAsInSourceProcessorOrSink} channels;

	@Autowired
	protected MessageCollector messageCollector;

	@WebIntegrationTest(value = "foo=bar")
	public static class SimpleMappingTests extends ${capitalizedNameOfModuleWithoutType}XXX${typeAsInSourceProcessorOrSink}IntegrationTests {

		@Test
		public void someIntegrationTest() {
			fail("you should rename the classes, removing 'XXX'");
			fail("you should enrich the README");
		}

	}

}
