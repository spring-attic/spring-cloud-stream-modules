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

package org.springframework.cloud.stream.module.cassandra.sink;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.EnvironmentTestUtils;
import org.springframework.cassandra.core.ConsistencyLevel;
import org.springframework.cassandra.core.RetryPolicy;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.cassandra.outbound.CassandraMessageHandler;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

/**
 * @author Thomas Risberg
 */
public class CassandraSinkPropertiesTests {
	
	@Test
	public void consistencyLevelCanBeCustomized() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(context, "consistency-level:" + ConsistencyLevel.LOCAL_QUOROM);
		context.register(Conf.class);
		context.refresh();
		CassandraSinkProperties properties = context.getBean(CassandraSinkProperties.class);
		assertThat(properties.getConsistencyLevel(), equalTo(ConsistencyLevel.LOCAL_QUOROM));
	}

	@Test
	public void retryPolicyCanBeCustomized() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(context, "retry-policy:" + RetryPolicy.DOWNGRADING_CONSISTENCY);
		context.register(Conf.class);
		context.refresh();
		CassandraSinkProperties properties = context.getBean(CassandraSinkProperties.class);
		assertThat(properties.getRetryPolicy(), equalTo(RetryPolicy.DOWNGRADING_CONSISTENCY));
	}

	@Test
	public void ttlCanBeCustomized() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(context, "ttl:" + 1000);
		context.register(Conf.class);
		context.refresh();
		CassandraSinkProperties properties = context.getBean(CassandraSinkProperties.class);
		assertThat(properties.getTtl(), equalTo(1000));
	}

	@Test
	public void queryTypeCanBeCustomized() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(context, "query-type:" + CassandraMessageHandler.Type.UPDATE);
		context.register(Conf.class);
		context.refresh();
		CassandraSinkProperties properties = context.getBean(CassandraSinkProperties.class);
		assertThat(properties.getQueryType(), equalTo(CassandraMessageHandler.Type.UPDATE));
	}

	@Test
	public void ingestQueryCanBeCustomized() {
		String query = "insert into book (isbn, title, author) values (?, ?, ?)";
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(context, "ingest-query:" + query);
		context.register(Conf.class);
		context.refresh();
		CassandraSinkProperties properties = context.getBean(CassandraSinkProperties.class);
		assertThat(properties.getIngestQuery(), equalTo(query));
	}

	@Test
	public void statementExpressionCanBeCustomized() {
		String queryDsl = "Select(FOO.BAR).From(FOO)";
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(context, "statement-expression:" + queryDsl);
		context.register(Conf.class);
		context.refresh();
		CassandraSinkProperties properties = context.getBean(CassandraSinkProperties.class);
		assertThat(properties.getStatementExpression(), equalTo(queryDsl));
	}

	@Configuration
	@EnableConfigurationProperties(CassandraSinkProperties.class)
	static class Conf {
	}

}
