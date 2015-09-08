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

import com.gemstone.gemfire.cache.DataPolicy;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.gemfire.client.ClientCacheFactoryBean;
import org.springframework.data.gemfire.client.ClientRegionFactoryBean;
import org.springframework.util.StringUtils;

/**
 * @author David Turanski
 */
@Configuration
@EnableConfigurationProperties(GemfireRegionProperties.class)
public class GemfireClientRegionConfiguration {

	@Autowired
	GemfireRegionProperties config;

	@Value("${spring.application.name:}")
	String regionName;

	@Bean
	public ClientCacheFactoryBean clientCache() {
		ClientCacheFactoryBean clientCacheFactoryBean = new ClientCacheFactoryBean();
		clientCacheFactoryBean.setUseBeanFactoryLocator(false);
		clientCacheFactoryBean.setPoolName("gemfirePool");
		return clientCacheFactoryBean;
	}

	@Bean(name = "clientRegion")
	public ClientRegionFactoryBean clientRegionFactoryBean() {
		ClientRegionFactoryBean clientRegionFactoryBean = new ClientRegionFactoryBean();
		clientRegionFactoryBean.setRegionName(StringUtils.hasText(config.getRegionName()) ? config.getRegionName() : 
				regionName);
		clientRegionFactoryBean.setDataPolicy(DataPolicy.EMPTY);
		try {
			clientRegionFactoryBean.setCache(clientCache().getObject());
		}
		catch (Exception e) {
			throw new BeanCreationException(e.getMessage(), e);
		}
		return clientRegionFactoryBean;
	}
}
