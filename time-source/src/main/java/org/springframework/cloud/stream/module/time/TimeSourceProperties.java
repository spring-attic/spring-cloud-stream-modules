/*
 * Copyright 2013-2016 the original author or authors.
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

package org.springframework.cloud.stream.module.time;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Describes properties of the {@code time} source module.
 *
 * @author Eric Bottard
 * @author Gary Russell
 * @author David Turanski
 *
 * @deprecated in favor of {@link org.springframework.cloud.stream.module.trigger.TriggerProperties#dateFormat}
 */
@Deprecated
@ConfigurationProperties
public class TimeSourceProperties {

	/**
	 * 	how to render the current time, using SimpleDateFormat
	 */
	private String format = "yyyy-MM-dd HH:mm:ss";

	@DateFormat
	public String getFormat() {
		return this.format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

}
