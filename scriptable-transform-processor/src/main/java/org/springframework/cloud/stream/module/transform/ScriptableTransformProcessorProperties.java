/*
 * Copyright 2016 the original author or authors.
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
package org.springframework.cloud.stream.module.transform;

import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the Scriptable Transform Processor module.
 *
 * @author Andy Clement
 */
@ConfigurationProperties
public class ScriptableTransformProcessorProperties {

	/**
	 * Language of the text in the script property. Supported: groovy, javascript, ruby.
	 */
	@NotNull
	private String language;

	/*
	 * Extra notes on the script parameter. The UI will typically look after encoding
	 * newlines and double quotes when packaging the value to pass to the script 
	 * property. If not using the UI, attempting to define
	 * a script directly in the shell for example, it is important to note:
	 * - newlines should be escaped (\\n)
	 * - a single " should be expressed in a pair "" - the DSL parser recognizes this pattern
	 * - If the script starts and ends with a " then they will be stripped off before treating what is
	 *   left as the script.
	 * 
	 * Examples:
	 * ruby: --script="return ""#{payload.upcase}"""
	 * javascript: --script="function double(a) {\\n return a+"" + ""+a;\\n}\\ndouble(payload);"
	 */
	/**
	 * Text of the script.
	 */
	@NotNull
	private String script;

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getScript() {
		return script;
	}

	public void setScript(String script) {
		this.script = script;
	}

}
