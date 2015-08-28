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

package org.springframework.cloud.task.module.timestamp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.OutputCapture;


/**
 * @author Glenn Renfro
 */
public class TimestampTaskIntegrationTests {

	@Rule
	public OutputCapture outputCapture = new OutputCapture();

	@Test
	public void testOverride() throws Exception {
		final String TEST_DATE_DOTS = ".......";
		final String SUCCESS_MESSAGE = "Job: [SimpleJob: [name=job]] completed with the following parameters: [{-format=yyyy.......}] and the following status: [COMPLETED]";
		String[] args = { "--format=yyyy" + TEST_DATE_DOTS };

		assertEquals(0, SpringApplication.exit(SpringApplication
				.run(TimestampTaskApplication.class, args)));
		String output = this.outputCapture.toString();
		assertTrue("Unable to find the timestamp: " + output,
				output.contains(TEST_DATE_DOTS));
		assertTrue("Test results do not show successful build run: " + output,
				output.contains(SUCCESS_MESSAGE));
	}
}

