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
import java.util.List;

import org.springframework.expression.EvaluationException;
import org.springframework.expression.Expression;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.messaging.Message;
import org.springframework.util.MultiValueMap;

/**
 * Jdbc Sink implementation for the case where the message payload represents one "line"
 * to be inserted. Several SpEL expressions can be tried against the message, typically to
 * support {@literal payload} as being an implicit evaluation root.
 *
 * @author Eric Bottard
 */
public class SimpleJdbcSink extends JdbcSink {

	public static final Object NOT_SET = new Object();

	private final MultiValueMap<String, Expression> columnExpressionVariations;

	public SimpleJdbcSink(JdbcOperations jdbcOperations, String tableName, MultiValueMap<String, Expression> columnExpressionVariations) {
		super(jdbcOperations, tableName, columnExpressionVariations.keySet());
		this.columnExpressionVariations = columnExpressionVariations;
	}

	@Override
	public void handle(final Message<?> message) {
		jdbcOperations.update(sql, new PreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps) throws SQLException {
				int index = 1;
				for (List<Expression> spels : columnExpressionVariations.values()) {
					Object value = NOT_SET;
					EvaluationException lastException = null;
					for (Expression spel : spels) {
						try {
							value = spel.getValue(message);
						}
						catch (EvaluationException e) {
							lastException = e;
						}
					}
					if (value != NOT_SET) {
						ps.setObject(index++, value);
					} else {
						throw lastException;
					}
				}
			}
		});
	}
}
