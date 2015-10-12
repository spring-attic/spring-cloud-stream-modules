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

import java.util.Set;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.EvaluationContext;
import org.springframework.integration.context.IntegrationContextUtils;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.messaging.Message;

/**
 * Abstract base class for Jdbc sink behavior.
 *
 * @author Eric Bottard
 */
public abstract class JdbcSink {
	protected final JdbcOperations jdbcOperations;

	protected final String sql;

	private final String tableName;

	protected EvaluationContext evaluationContext;

	protected JdbcSink(JdbcOperations jdbcOperations, String tableName, Set<String> columns) {
		this.jdbcOperations = jdbcOperations;
		this.tableName = tableName;
		this.sql = generateSql(columns);
	}

	@Autowired
	public void setBeanFactory(BeanFactory beanFactory) {
		this.evaluationContext = IntegrationContextUtils.getEvaluationContext(beanFactory);
	}

	private String generateSql(Set<String> columns) {
		StringBuilder builder = new StringBuilder("INSERT INTO ");
		StringBuilder questionMarks = new StringBuilder(") VALUES (");
		builder.append(this.tableName).append("(");
		int i = 0;

		for (String column : columns) {
			if (i++ > 0) {
				builder.append(", ");
				questionMarks.append(", ");
			}
			builder.append(column);
			questionMarks.append('?');
		}
		builder.append(questionMarks).append(")");
		return builder.toString();
	}

	abstract void handle(Message<?> message);
}
