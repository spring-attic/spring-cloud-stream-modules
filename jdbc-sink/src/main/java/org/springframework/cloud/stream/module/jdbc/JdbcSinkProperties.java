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

import java.util.Collections;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Holds configuration properties for the Jdbc Sink module.
 *
 * @author Eric Bottard
 */
@ConfigurationProperties
public class JdbcSinkProperties {

	/**
	 * The name of the table to write into.
	 */
	@Value("${spring.application.name:messages}")
	private String tableName;

	/**
	 * The names of the columns that shall receive data, as a set of column[:SpEL] mappings.
	 * Also used at initialization time to issue the DDL.
	 */
	private Map<String, String> columns = Collections.singletonMap("payload", "payload.toString()");

	/**
	 * Whether to issue a DDL command to create the table when the module starts.
	 */
	private boolean initializeDatabase = false;

	/**
	 * If set, then the received payload must be a collection and this tells how
	 * frequently to emit batches of inserts.
	 */
	private Integer batchSize = null;

	public Integer getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(Integer batchSize) {
		this.batchSize = batchSize;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public Map<String, String> getColumns() {
		return columns;
	}

	@SupportsShorthands
	public void setColumns(Map<String, String> columns) {
		this.columns = columns;
	}

	public boolean isInitializeDatabase() {
		return initializeDatabase;
	}

	public void setInitializeDatabase(boolean initializeDatabase) {
		this.initializeDatabase = initializeDatabase;
	}
}
