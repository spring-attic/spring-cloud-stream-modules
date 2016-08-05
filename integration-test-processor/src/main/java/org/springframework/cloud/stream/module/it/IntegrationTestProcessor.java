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

package org.springframework.cloud.stream.module.it;

import static org.springframework.cloud.stream.module.it.IntegrationTestProcessorProperties.FUNNY_CHARACTERS;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * A module that can misbehave, useful for integration testing of module deployers.
 *
 * @author Eric Bottard
 */
@EnableConfigurationProperties(IntegrationTestProcessorProperties.class)
public class IntegrationTestProcessor {

	@Autowired
	private IntegrationTestProcessorProperties properties;

	@PostConstruct
	public void init() throws InterruptedException {
		String parameterThatMayNeedEscaping = properties.getParameterThatMayNeedEscaping();
		if (parameterThatMayNeedEscaping != null && !FUNNY_CHARACTERS.equals(parameterThatMayNeedEscaping)) {
			throw new IllegalArgumentException(String.format("Expected value to be equal to '%s', but was '%s'", FUNNY_CHARACTERS, parameterThatMayNeedEscaping));
		}


		if (properties.getMatchInstances().isEmpty() || properties.getMatchInstances().contains(properties.getInstanceIndex())) {
			Thread.sleep(properties.getInitDelay());
			if (properties.getKillDelay() >= 0) {
				new Thread() {

					@Override
					public void run() {
						try {
							Thread.sleep(properties.getKillDelay());
							System.exit(1);
						}
						catch (InterruptedException e) {
							throw new RuntimeException(e);
						}
					}
				}.start();
			}
		}
	}

}
