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

package org.springframework.cloud.stream.module.common;

import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.integration.scripting.DefaultScriptVariableGenerator;
import org.springframework.integration.scripting.ScriptVariableGenerator;

/**
 * An AutoConfiguration that exposes a {@link ScriptVariableGenerator} to customize a script.
 *
 * <p>Only triggers on the presence of a {@code script} Environment property.</p>
 *
 * @author David Turanski
 * @author Eric Bottard
 */
@Configuration
@ConditionalOnProperty("script")
@ConfigurationProperties
public class ScriptModuleVariableConfiguration {

	/**
	 * Variable bindings as a comma delimited string of name-value pairs, e.g. 'foo=bar,baz=car'.
	 */
	private String variables;

	/**
	 * The location of a properties file containing custom script variable bindings.
	 */
	private Resource propertiesLocation;

	@Bean(name = "variableGenerator")
	@SuppressWarnings("unchecked")
	ScriptVariableGenerator scriptVariableGenerator() throws IOException {
		return new DefaultScriptVariableGenerator(scriptVariables());
	}

	@SuppressWarnings("unchecked")
	private Map scriptVariables() {
		ScriptModulePropertiesFactoryBean scriptModulePropertiesFactoryBean = new ScriptModulePropertiesFactoryBean();
		scriptModulePropertiesFactoryBean.setVariables(variables);
		if (propertiesLocation != null) {
			scriptModulePropertiesFactoryBean.setLocation(propertiesLocation);
		}
		try {
			scriptModulePropertiesFactoryBean.afterPropertiesSet();
			return scriptModulePropertiesFactoryBean.getObject();
		}
		catch (IOException e) {
			throw new BeanCreationException(e.getMessage());
		}
	}

	public void setVariables(String variables) {
		this.variables = variables;
	}

	public void setPropertiesLocation(Resource propertiesLocation) {
		this.propertiesLocation = propertiesLocation;
	}
}
