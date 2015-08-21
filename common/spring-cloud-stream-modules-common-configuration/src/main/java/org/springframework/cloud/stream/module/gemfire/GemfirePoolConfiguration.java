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

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.gemfire.client.PoolFactoryBean;
import org.springframework.data.gemfire.config.GemfireConstants;

/**
 * @author David Turanski
 */
@Configuration
@EnableConfigurationProperties(GemfirePoolProperties.class)
@Import(InetSocketAddressConverterConfiguration.class)
public class GemfirePoolConfiguration {

	@Autowired
	GemfirePoolProperties config;

	@Bean
	public PoolFactoryBean gemfirePool() {
		PoolFactoryBean poolFactoryBean = new PoolFactoryBean();

		switch (config.getConnectType()) {
			case locator:
				poolFactoryBean.setLocators(Arrays.asList(config.getHostAddresses()));
				break;
			case server:
				poolFactoryBean.setServers(Arrays.asList(config.getHostAddresses()));
				break;
			default:
				throw new IllegalArgumentException("connectType " + config.getConnectType() + " is not supported.");
		}
		poolFactoryBean.setSubscriptionEnabled(config.isSubscriptionEnabled());
		poolFactoryBean.setName(GemfireConstants.DEFAULT_GEMFIRE_POOL_NAME);
		return poolFactoryBean;
	}
}
