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

package org.springframework.cloud.stream.module.jdbc;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ResourceLoader;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.messaging.Message;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * A module that writes its incoming payload to an RDBMS using JDBC.
 *
 * @author Eric Bottard
 */
@EnableBinding(Sink.class)
@EnableConfigurationProperties(JdbcSinkProperties.class)
public class JdbcSinkConfiguration {

	private static final Logger logger = LoggerFactory.getLogger(JdbcSinkConfiguration.class);

	@Autowired(required = false)
	private SpelExpressionParser spelExpressionParser = new SpelExpressionParser();

	@Autowired
	private JdbcSinkProperties properties;

	@Autowired
	private JdbcSink jdbcSink;

	@Autowired
	private GenericApplicationContext applicationContext;


	@ServiceActivator(autoStartup = "false", inputChannel = Sink.INPUT)
	public void handleMessage(Message<?> msg) {
		jdbcSink.handle(msg);
	}

	@Bean
	public JdbcSink jdbcSink(JdbcOperations jdbcOperations) {
		if (properties.getBatchSize() != null) {
			Map<String, Expression> columnExpressionVariations = new HashMap<>();
			for (Map.Entry<String, String> entry : properties.getColumns().entrySet()) {
				String value = entry.getValue();
				columnExpressionVariations.put(entry.getKey(), spelExpressionParser.parseExpression(value));
			}
			return new BatchingJdbcSink(jdbcOperations, properties.getTableName(), columnExpressionVariations, properties.getBatchSize());
		} // Implicitly allow expressions against the (single item) payload
		else {
			MultiValueMap<String, Expression> columnExpressionVariations = new LinkedMultiValueMap<>();
			for (Map.Entry<String, String> entry : properties.getColumns().entrySet()) {
				String value = entry.getValue();
				columnExpressionVariations.add(entry.getKey(), spelExpressionParser.parseExpression(value));
				if (!value.startsWith("payload")) {
					columnExpressionVariations.add(entry.getKey(), spelExpressionParser.parseExpression("payload." + value));
				}
			}
			return new SimpleJdbcSink(jdbcOperations, properties.getTableName(), columnExpressionVariations);
		}
	}

	@ConditionalOnProperty("initialize")
	@Bean
	public DataSourceInitializer nonBootDataSourceInitializer(DataSource dataSource, ResourceLoader resourceLoader) {
		DataSourceInitializer dataSourceInitializer = new DataSourceInitializer();
		dataSourceInitializer.setDataSource(dataSource);
		ResourceDatabasePopulator databasePopulator = new ResourceDatabasePopulator();
		databasePopulator.setIgnoreFailedDrops(true);
		dataSourceInitializer.setDatabasePopulator(databasePopulator);
		if ("true".equals(properties.getInitialize())) {
			databasePopulator.addScript(new DefaultInitializationScriptResource(properties));
		} else {
			databasePopulator.addScript(resourceLoader.getResource(properties.getInitialize()));
		}
		return dataSourceInitializer;
	}

	@Bean
	public JdbcOperations jdbcOperations(DataSource dataSource) {
		return new JdbcTemplate(dataSource);
	}

	/*
	 * This is needed to prevent a circular dependency issue with the creation of the converter.
	 */
	public static class Nested {
		@Bean
		@ConfigurationPropertiesBinding
		public ShorthandMapConverter shorthandMapConverter() {
			return new ShorthandMapConverter();
		}
	}

}
