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

package org.springframework.cloud.stream.module.dataset.sink;

import org.kitesdk.data.PartitionStrategy;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.hadoop.store.StoreException;
import org.springframework.data.hadoop.store.dataset.DatasetDefinition;
import org.springframework.data.hadoop.store.dataset.DatasetOperations;
import org.springframework.data.hadoop.store.dataset.DatasetRepositoryFactory;
import org.springframework.data.hadoop.store.dataset.DatasetTemplate;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.SpelParseException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.integration.aggregator.DefaultAggregatingMessageGroupProcessor;
import org.springframework.integration.aggregator.ExpressionEvaluatingCorrelationStrategy;
import org.springframework.integration.aggregator.ExpressionEvaluatingReleaseStrategy;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.AggregatorFactoryBean;
import org.springframework.integration.store.MessageGroupStore;
import org.springframework.integration.store.MessageGroupStoreReaper;
import org.springframework.integration.store.SimpleMessageStore;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Configuration class for the HDFS DatasetSink.
 * <p/>
 * The configuration contains the property 'fsUri' to configure a connection to HDFS as well as the
 * additional properties for the sink like directory, fileName, codec etc. You can also use the
 * standard 'spring.hadoop.fsUri' property for specifying the HDFS connection.
 *
 * @author Thomas Risberg
 */
@Configuration
@EnableScheduling
@EnableConfigurationProperties(DatasetSinkProperties.class)
public class DatasetSinkConfiguration {

	@Autowired
	private DatasetSinkProperties properties;

	@Bean
	public MessageChannel toSink() {
		return new DirectChannel();
	}

	@Bean
	@ServiceActivator(inputChannel= Sink.INPUT)
	FactoryBean<MessageHandler> aggregatorFactoryBean(MessageChannel toSink, MessageGroupStore messageGroupStore) {
		AggregatorFactoryBean aggregatorFactoryBean = new AggregatorFactoryBean();
		aggregatorFactoryBean.setCorrelationStrategy(
				new ExpressionEvaluatingCorrelationStrategy("payload.getClass().name"));
		String releaseStrategyExpression = "size() == " + properties.getBatchSize();
		aggregatorFactoryBean.setReleaseStrategy(
				new ExpressionEvaluatingReleaseStrategy(releaseStrategyExpression));
		aggregatorFactoryBean.setMessageStore(messageGroupStore);
		aggregatorFactoryBean.setProcessorBean(new DefaultAggregatingMessageGroupProcessor());
		aggregatorFactoryBean.setExpireGroupsUponCompletion(true);
		aggregatorFactoryBean.setSendPartialResultOnExpiry(true);
		aggregatorFactoryBean.setOutputChannel(toSink);
		return aggregatorFactoryBean;
	}

	@Bean
	MessageGroupStore messageGroupStore() {
		SimpleMessageStore messageGroupStore = new SimpleMessageStore();
		messageGroupStore.setTimeoutOnIdle(true);
		messageGroupStore.setCopyOnGet(false);
		return messageGroupStore;
	}

	@Bean
	MessageGroupStoreReaper messageGroupStoreReaper(MessageGroupStore messageStore) {
		MessageGroupStoreReaper messageGroupStoreReaper = new MessageGroupStoreReaper(messageStore);
			messageGroupStoreReaper.setTimeout(properties.getIdleTimeout());
		messageGroupStoreReaper.setAutoStartup(true);
		messageGroupStoreReaper.setExpireOnDestroy(true);
		return messageGroupStoreReaper;
	}

	@Bean
	Reaper reaper() {
		return new Reaper();
	}

	@Bean
	public DatasetOperations datasetOperations(DatasetRepositoryFactory datasetRepositoryFactory,
	                                           DatasetDefinition datasetDefinition) {
		return new DatasetTemplate(datasetRepositoryFactory, datasetDefinition);
	}

	@Bean
	public DatasetRepositoryFactory datasetRepositoryFactory(org.apache.hadoop.conf.Configuration configuration) {
		DatasetRepositoryFactory datasetRepositoryFactory = new DatasetRepositoryFactory();
		datasetRepositoryFactory.setConf(configuration);
		datasetRepositoryFactory.setBasePath(properties.getDirectory());
		datasetRepositoryFactory.setNamespace(properties.getNamespace());
		return datasetRepositoryFactory;
	}

	@Bean
	public DatasetDefinition datasetDefinition() {

		DatasetDefinition datasetDefinition =
				new DatasetDefinition(properties.isAllowNullValues(), properties.getFormat());
		if (StringUtils.hasText(properties.getPartitionPath())) {
			datasetDefinition.setPartitionStrategy(parsePartitionExpression(properties.getPartitionPath()));
		}
		if (properties.getWriterCacheSize() > 0) {
			datasetDefinition.setWriterCacheSize(properties.getWriterCacheSize());
		}
		if (StringUtils.hasText(properties.getCompressionType())) {
			datasetDefinition.setCompressionType(properties.getCompressionType());
		}
		return datasetDefinition;
	}

	private static PartitionStrategy parsePartitionExpression(String expression) {

		List<String> expressions = Arrays.asList(expression.split("/"));

		ExpressionParser parser = new SpelExpressionParser();
		PartitionStrategy.Builder psb = new PartitionStrategy.Builder();
		StandardEvaluationContext ctx = new StandardEvaluationContext(psb);
		for (String expr : expressions) {
			try {
				Expression e = parser.parseExpression(expr);
				psb = e.getValue(ctx, PartitionStrategy.Builder.class);
			}
			catch (SpelParseException spe) {
				if (!expr.trim().endsWith(")")) {
					throw new StoreException("Invalid partitioning expression '" + expr
							+ "' -  did you forget the closing parenthesis?", spe);
				}
				else {
					throw new StoreException("Invalid partitioning expression '" + expr + "'!", spe);
				}
			}
			catch (SpelEvaluationException see) {
				throw new StoreException("Invalid partitioning expression '" + expr + "' - failed evaluation!", see);
			}
			catch (NullPointerException npe) {
				throw new StoreException("Invalid partitioning expression '" + expr + "' - was evaluated to null!", npe);
			}
		}
		return psb.build();
	}

	public static class Reaper {

		@Autowired
		MessageGroupStoreReaper messageGroupStoreReaper;

		@Scheduled(fixedRate=1000)
		public void reap() {
			messageGroupStoreReaper.run();
		}

	}
}
