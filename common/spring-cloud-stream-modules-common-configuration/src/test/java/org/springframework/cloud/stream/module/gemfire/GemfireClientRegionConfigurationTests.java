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

package org.springframework.cloud.stream.module.gemfire;


import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import javax.annotation.Resource;

import com.gemstone.gemfire.cache.Region;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author David Turanski
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {GemfireClientRegionConfiguration.class, GemfirePoolConfiguration.class,
		PropertyPlaceholderAutoConfiguration.class})
@IntegrationTest({"spring.application.name=foo"})
@DirtiesContext
public abstract class GemfireClientRegionConfigurationTests {

	@Autowired
	protected ApplicationContext context;

	@Resource(name = "clientRegion")
	Region<?, ?> region;

	public static class DefaultRegionTests extends GemfireClientRegionConfigurationTests {
		@Test
		public void test() {
			assertThat(region.getName(), equalTo("foo"));
		}
	}

	@IntegrationTest({"regionName=bar"})
	public static class AssignRegionNameTests extends GemfireClientRegionConfigurationTests {
		@Test
		public void test() {
			assertThat(region.getName(), equalTo("bar"));
		}
	}
}

