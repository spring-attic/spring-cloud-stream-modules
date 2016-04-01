/*
 * Copyright 2015-2016 the original author or authors.
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

import java.util.Collections;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.gemfire.outbound.CacheWritingMessageHandler;

import com.gemstone.gemfire.cache.Region;

/**
 * @author David Turanski
 */
@EnableBinding(Sink.class)
@Import({GemfirePoolConfiguration.class, GemfireClientRegionConfiguration.class})
@EnableConfigurationProperties(GemfireSinkProperties.class)
public class GemfireSink {

	@Autowired
	GemfireSinkProperties config;

	@Autowired
	ApplicationContext context;

	//NOTE: https://jira.spring.io/browse/SPR-7915 supposedly fixed in SF 4.3. So
	//should be able to change to @Autowired at that point
	@Resource(name = "clientRegion")
	Region<String, ?> region;

	@Bean
	@ServiceActivator(inputChannel = Sink.INPUT)
	public CacheWritingMessageHandler messageHandler() {
		CacheWritingMessageHandler messageHandler = new CacheWritingMessageHandler(this.region);
		messageHandler.setCacheEntries(Collections.singletonMap(this.config.getKeyExpression(), "payload"));
		return messageHandler;
	}

}
