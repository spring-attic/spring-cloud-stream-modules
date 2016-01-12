/*
 * Copyright 2016 the original author or authors.
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

package org.springframework.cloud.stream.module.jdbc.source;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.Bindings;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlowBuilder;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.SourcePollingChannelAdapterSpec;
import org.springframework.integration.dsl.channel.MessageChannels;
import org.springframework.integration.dsl.core.Pollers;
import org.springframework.integration.dsl.support.Consumer;
import org.springframework.integration.jdbc.JdbcPollingChannelAdapter;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.concurrent.TimeUnit;

/**
 * A module that reads data from an RDBMS using JDBC and creates a payload with the data.
 *
 * @author Thomas Risberg
 */
@EnableBinding(Source.class)
@EnableConfigurationProperties(JdbcSourceProperties.class)
public class JdbcSourceConfiguration {

	@Autowired
	private JdbcSourceProperties properties;

	@Autowired
	private DataSource dataSource;

	@Autowired
	private PlatformTransactionManager transactionManager;

	@Autowired
	@Bindings(JdbcSourceConfiguration.class)
	private Source source;

	@Bean
	public TaskExecutor taskExecutor() {
		return new ThreadPoolTaskExecutor();
	}

	@Bean
	@ConditionalOnProperty(value = "split", havingValue = "true", matchIfMissing = true)
	public IntegrationFlow splitFlow() {
		return IntegrationFlows.from("splitter")
				.split("payload")
				.channel(MessageChannels.executor(this.taskExecutor()))
				.channel(source.output())
				.get();
	}

	@Bean
	public MessageSource<Object> jdbcMessageSource() {
		JdbcPollingChannelAdapter jdbcPollingChannelAdapter =
				new JdbcPollingChannelAdapter(dataSource, properties.getQuery());
		jdbcPollingChannelAdapter.setMaxRowsPerPoll(properties.getMaxRowsPerPoll());
		if (properties.getUpdate() != null) {
			jdbcPollingChannelAdapter.setUpdateSql(properties.getUpdate());
		}
		return jdbcPollingChannelAdapter;
	}

	@Bean
	public PollerMetadata poller() {
		return Pollers.fixedRate(properties.getFixedDelay(), TimeUnit.SECONDS).
				maxMessagesPerPoll(properties.getMaxMessages()).
				transactional(transactionManager).get();
	}

	@Bean
	public IntegrationFlow pollingFlow() {
		IntegrationFlowBuilder flowBuilder = IntegrationFlows.from(jdbcMessageSource(),
				new Consumer<SourcePollingChannelAdapterSpec>() {
					@Override
					public void accept(SourcePollingChannelAdapterSpec sourcePollingChannelAdapterSpec) {
						sourcePollingChannelAdapterSpec
								.autoStartup(false)
								.poller(poller());
					}
				});
		if (properties.isSplit()) {
			flowBuilder.channel("splitter");
		}
		else {
			flowBuilder.channel(source.output());
		}
		return flowBuilder.get();
	}
}
