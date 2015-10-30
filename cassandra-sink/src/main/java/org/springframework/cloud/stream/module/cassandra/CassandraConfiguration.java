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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cassandra.config.CompressionType;
import org.springframework.cassandra.core.keyspace.CreateKeyspaceSpecification;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.cassandra.config.SchemaAction;
import org.springframework.data.cassandra.config.java.AbstractCassandraConfiguration;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.datastax.driver.core.AuthProvider;
import com.datastax.driver.core.PlainTextAuthProvider;

/**
 * @author Artem Bilan
 * @author Thomas Risberg
 */
@Configuration
@EnableConfigurationProperties(CassandraProperties.class)
public class CassandraConfiguration extends AbstractCassandraConfiguration {

	@Autowired
	CassandraProperties cassandraProperties;

	@Override
	protected List<String> getStartupScripts() {
		if (cassandraProperties.getInitScript() == null) {
			return Collections.emptyList();
		}
		else {
			Resource initScriptResource = new DefaultResourceLoader().getResource(cassandraProperties.getInitScript());
			String scripts = null;
			try (Scanner scanner = new Scanner(initScriptResource.getInputStream(), "UTF-8")) {
				scripts = scanner.useDelimiter("\\A").next();
			} catch (IOException e) {
				throw new InvalidDataAccessApiUsageException("Unable to load the specified init script (" +
						cassandraProperties.getInitScript() + ")", e);
			}
			List<String> scriptList = new ArrayList<>();
			for (String script : StringUtils.delimitedListToStringArray(scripts, ";", "\r\n\f")) {
				if (StringUtils.hasText(script)) { // an empty String after the last ';'
					scriptList.add(script + ";");
				}
			}
			return scriptList;
		}
	}

	@Override
	protected String getContactPoints() {
		return cassandraProperties.getContactPoints();
	}

	@Override
	protected int getPort() {
		return cassandraProperties.getPort();
	}

	@Override
	protected String getKeyspaceName() {
		return cassandraProperties.getKeyspace();
	}

	@Override
	protected AuthProvider getAuthProvider() {
		if (StringUtils.hasText(cassandraProperties.getUsername())) {
			return new PlainTextAuthProvider(cassandraProperties.getUsername(), cassandraProperties.getPassword());
		}
		else {
			return null;
		}
	}

	@Override
	public SchemaAction getSchemaAction() {
		return !ObjectUtils.isEmpty(this.getEntityBasePackages()) ? SchemaAction.CREATE : super.getSchemaAction();
	}

	@Override
	public String[] getEntityBasePackages() {
		return cassandraProperties.getEntityBasePackages();
	}

	@Override
	protected CompressionType getCompressionType() {
		return cassandraProperties.getCompressionType();
	}

	@Override
	protected boolean getMetricsEnabled() {
		return cassandraProperties.isMetricsEnabled();
	}

	@Override
	protected List<CreateKeyspaceSpecification> getKeyspaceCreations() {
		if (ObjectUtils.isEmpty(getEntityBasePackages()) || cassandraProperties.getInitScript() != null) {
			return Collections.singletonList(CreateKeyspaceSpecification.createKeyspace(getKeyspaceName())
					.withSimpleReplication()
					.ifNotExists());
		}
		else {
			return super.getKeyspaceCreations();
		}
	}

}

