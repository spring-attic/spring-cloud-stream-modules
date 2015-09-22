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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.convert.ConversionService;
import org.springframework.expression.Expression;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;
import org.springframework.messaging.Message;

/**
 * JdbcSink implementation that uses batching. The payload must be convertible to
 * a collection, and there is only one Expression candidate per "line".
 *
 * @author Eric Bottard
 */
public class BatchingJdbcSink extends JdbcSink {

	private final Map<String, Expression> columnExpressions;

	private final int batchSize;

	@Autowired
	@Qualifier("integrationConversionService")
	private ConversionService conversionService;

	public BatchingJdbcSink(JdbcOperations jdbcOperations, String tableName, Map<String, Expression> columnExpressions, int batchSize) {
		super(jdbcOperations, tableName, columnExpressions.keySet());
		this.columnExpressions = columnExpressions;
		this.batchSize = batchSize;
	}

	@Override
	public void handle(Message<?> message) {
		Collection<Object> lines = adaptToCollection(message);
		jdbcOperations.batchUpdate(sql, lines, batchSize, new ParameterizedPreparedStatementSetter<Object>() {
			@Override
			public void setValues(PreparedStatement ps, Object argument) throws SQLException {
				int index = 1;
				for (Expression spel : columnExpressions.values()) {
					Object value = spel.getValue(argument);
					ps.setObject(index++, value);
				}
			}
		});
	}

	private Collection<Object> adaptToCollection(Message<?> message) {
		return conversionService.convert(message.getPayload(), Collection.class);
	}

}
