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

package org.springframework.cloud.stream.module.cassandra;

import javax.validation.constraints.AssertFalse;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Range;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cassandra.config.CassandraCqlClusterFactoryBean;
import org.springframework.cassandra.config.CompressionType;
import org.springframework.util.StringUtils;

/**
 * Properties for the cassandra sink.
 *
 * @author Artem Bilan
 * @author Thomas Risberg
 */
@ConfigurationProperties("spring.cassandra")
public class CassandraProperties {

	/**
	 * The comma-delimited string of the hosts to connect to Cassandra.
	 */
	private String contactPoints = CassandraCqlClusterFactoryBean.DEFAULT_CONTACT_POINTS;

	/**
	 * The port to use to connect to the Cassandra host.
	 */
	private int port = CassandraCqlClusterFactoryBean.DEFAULT_PORT;

	/**
	 * The keyspace name to connect to.
	 */
	@Value("${spring.application.name:tmp}")
	private String keyspace;

	/**
	 * The username for connection.
	 */
	private String username;

	/**
	 * The password for connection.
	 */
	private String password;

	/**
	 * The path to file with CQL scripts (delimited by ';') to initialize keyspace schema.
	 */
	private String initScript;

	/**
	 * The base packages to scan for entities annotated with Table annotations.
	 */
	private String[] entityBasePackages = new String[0];

	/**
	 * The compression to use for the transport.
	 */
	private CompressionType compressionType = CompressionType.NONE;

	/**
	 * Enable/disable metrics collection for the created cluster.
	 */
	private boolean metricsEnabled = CassandraCqlClusterFactoryBean.DEFAULT_METRICS_ENABLED;

	public void setContactPoints(String contactPoints) {
		this.contactPoints = contactPoints;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setKeyspace(String keyspace) {
		this.keyspace = keyspace;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setInitScript(String initScript) {
		this.initScript = initScript;
	}

	public void setEntityBasePackages(String[] entityBasePackages) {
		this.entityBasePackages = entityBasePackages;
	}

	public void setCompressionType(CompressionType compressionType) {
		this.compressionType = compressionType;
	}

	public void setMetricsEnabled(boolean metricsEnabled) {
		this.metricsEnabled = metricsEnabled;
	}

	@NotNull
	public String getContactPoints() {
		return this.contactPoints;
	}

	@Range(min = 0, max = 65535)
	public int getPort() {
		return this.port;
	}

	public String getKeyspace() {
		return this.keyspace;
	}

	public String getUsername() {
		return this.username;
	}

	public String getPassword() {
		return this.password;
	}

	public String getInitScript() {
		return this.initScript;
	}

	public String[] getEntityBasePackages() {
		return this.entityBasePackages;
	}

	@NotNull
	public CompressionType getCompressionType() {
		return this.compressionType;
	}

	public boolean isMetricsEnabled() {
		return this.metricsEnabled;
	}

	@AssertFalse(message = "both 'username' and 'password' are required or neither one")
	private boolean isInvalid() {
		return StringUtils.hasText(this.username) ^ StringUtils.hasText(this.password);
	}

}
