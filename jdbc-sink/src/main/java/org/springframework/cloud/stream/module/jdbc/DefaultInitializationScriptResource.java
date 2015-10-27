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

import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.core.io.ByteArrayResource;

/**
 * An in-memory script crafted for dropping-creating the table we're working with.
 * All columns are created as VARCHAR(2000).
 *
 * @author Eric Bottard
 */
public class DefaultInitializationScriptResource extends ByteArrayResource {

	private static final Logger logger = LoggerFactory.getLogger(DefaultInitializationScriptResource.class);

	public DefaultInitializationScriptResource(JdbcSinkProperties properties) {
		super(scriptFor(properties).getBytes(Charset.forName("UTF-8")));
	}

	private static String scriptFor(JdbcSinkProperties properties) {
		StringBuilder result = new StringBuilder("DROP TABLE ");
		result.append(properties.getTableName()).append(";\n\n");

		result.append("CREATE TABLE ").append(properties.getTableName()).append('(');
		int i = 0;
		for (String column : properties.getColumns().keySet()) {
			if (i++ > 0) {
				result.append(", ");
			}
			result.append(column).append(" VARCHAR(2000)");
		}
		result.append(");\n");
		logger.debug("Generated the following initializing script for table {}:\n{}", properties.getTableName(), result);
		return result.toString();
	}
}
