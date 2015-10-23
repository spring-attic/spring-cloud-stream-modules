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
package org.springframework.cloud.stream.module.file;

import java.util.Collections;

import org.springframework.integration.dsl.IntegrationFlowBuilder;
import org.springframework.integration.dsl.file.Files;
import org.springframework.integration.dsl.support.Transformers;
import org.springframework.messaging.MessageHeaders;

/**
 * @author Gary Russell
 *
 */
public class FileUtils {

	/**
	 * Enhance an {@link IntegrationFlowBuilder} to add flow snippets, depending on
	 * {@link FileConsumerProperties}.
	 * @param flowBuilder the flow builder.
	 * @param fileConsumerProperties the properties.
	 * @return the updated flow builder.
	 */
	public static IntegrationFlowBuilder enhanceFlowForReadingMode(IntegrationFlowBuilder flowBuilder,
			FileConsumerProperties fileConsumerProperties) {
		switch (fileConsumerProperties.getMode()) {
		case contents:
			flowBuilder.enrichHeaders(Collections.<String, Object>singletonMap(MessageHeaders.CONTENT_TYPE,
					"application/octet-stream"))
					.transform(Transformers.fileToByteArray());
			break;
		case lines:
			flowBuilder.enrichHeaders(Collections.<String, Object>singletonMap(MessageHeaders.CONTENT_TYPE,
					"text/plain"))
					.split(Files.splitter(true, fileConsumerProperties.getWithMarkers()));
		case ref:
			break;
		default:
			throw new IllegalArgumentException(fileConsumerProperties.getMode().name() +
					" is not a supported file reading mode.");
		}
		return flowBuilder;
	}

}
