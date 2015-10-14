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
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cassandra.config.CompressionType;
import org.springframework.cassandra.core.CqlTemplate;
import org.springframework.cassandra.core.keyspace.CreateKeyspaceSpecification;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.data.cassandra.config.SchemaAction;
import org.springframework.data.cassandra.config.java.AbstractCassandraConfiguration;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.datastax.driver.core.AuthProvider;
import com.datastax.driver.core.PlainTextAuthProvider;
import com.datastax.driver.core.Session;

/**
 * @author Artem Bilan
 * @author Thomas Risberg
 */
@Configuration
@EnableConfigurationProperties(CassandraProperties.class)
public class CassandraConfiguration extends AbstractCassandraConfiguration {

	@Autowired
	CassandraProperties cassandraSinkProperties;
	
	@Autowired
	private Session session;

	@PostConstruct
	public void init() throws IOException {
		if (cassandraSinkProperties.getInitScript() != null) {
			Resource initScriptResource = new DefaultResourceLoader().getResource(cassandraSinkProperties.getInitScript());
			Scanner scanner = new Scanner(initScriptResource.getInputStream(), "UTF-8");
			String scripts = null;
			try {
				scripts = scanner.useDelimiter("\\A").next();
			}
			finally {
				if (scanner != null) {
					scanner.close();
				}
			}

			CqlTemplate template = new CqlTemplate(this.session);

			for (String script : StringUtils.delimitedListToStringArray(scripts, ";", "\r\n\f")) {
				if (StringUtils.hasText(script)) { // an empty String after the last ';'
					template.execute(script + ";");
				}
			}
		}
	}

	@Override
	protected String getContactPoints() {
		return cassandraSinkProperties.getContactPoints();
	}

	@Override
	protected int getPort() {
		return cassandraSinkProperties.getPort();
	}

	@Override
	protected String getKeyspaceName() {
		return cassandraSinkProperties.getKeyspace();
	}

	@Override
	protected AuthProvider getAuthProvider() {
		if (StringUtils.hasText(cassandraSinkProperties.getUsername())) {
			return new PlainTextAuthProvider(cassandraSinkProperties.getUsername(), cassandraSinkProperties.getPassword());
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
		return cassandraSinkProperties.getEntityBasePackages();
	}

	@Override
	protected CompressionType getCompressionType() {
		return cassandraSinkProperties.getCompressionType();
	}

	@Override
	protected boolean getMetricsEnabled() {
		return cassandraSinkProperties.isMetricsEnabled();
	}

	@Override
	protected List<CreateKeyspaceSpecification> getKeyspaceCreations() {
		if (ObjectUtils.isEmpty(getEntityBasePackages()) || cassandraSinkProperties.getInitScript() != null) {
			return Collections.singletonList(CreateKeyspaceSpecification.createKeyspace(getKeyspaceName())
					.withSimpleReplication()
					.ifNotExists());
		}
		else {
			return super.getKeyspaceCreations();
		}
	}

}

