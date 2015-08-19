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
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PropertiesLoaderUtils;
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
@EnableConfigurationProperties(ScriptModuleVariablesProperties.class)
public class ScriptModuleVariableConfiguration {

	@Autowired
	private ScriptModuleVariablesProperties config;

	@Bean(name = "variableGenerator")
	@SuppressWarnings("unchecked")
	public ScriptVariableGenerator scriptVariableGenerator() throws IOException {
		Properties properties = new Properties();
		properties.putAll(config.getVariables());
		if (config.getPropertiesLocation()!= null) {
			PropertiesLoaderUtils.fillProperties(properties, config.getPropertiesLocation());
		}

		return new DefaultScriptVariableGenerator((Map) properties);
	}

}