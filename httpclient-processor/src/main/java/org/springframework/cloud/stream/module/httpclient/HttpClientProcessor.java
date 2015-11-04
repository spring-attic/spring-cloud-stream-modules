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

package org.springframework.cloud.stream.module.httpclient;

import java.net.URI;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.web.client.RestTemplate;

/**
 * A processor module that makes requests to an HTTP resource and emits the 
 * response body as a message payload. This processor can be combined, e.g., 
 * with a time source module to periodically poll results from a HTTP resource.
 *
 * @author Waldemar Hummer
 * @author Mark Fisher
 */
@MessageEndpoint
public class HttpClientProcessor {

	private static final Logger LOG = LoggerFactory.getLogger(HttpClientProcessor.class);

	@Autowired
	private HttpClientProcessorProperties properties;

	@Autowired
	private RestTemplate restTemplate;

	@ServiceActivator(inputChannel = Processor.INPUT, outputChannel = Processor.OUTPUT)
	public Object transform(Message<?> message) {
		try {
			/* construct headers */
			HttpHeaders headers = new HttpHeaders();
			if (properties.getHeadersExpression() != null) {
				Map<?, ?> headersMap = properties.getHeadersExpression().getValue(message, Map.class);
				for (Entry<?, ?> header : headersMap.entrySet()) {
					if (header.getKey() != null && header.getValue() != null) {
						headers.add(header.getKey().toString(),
								header.getValue().toString());
					}
				}
			}

			Class<?> responseType = properties.getExpectedReturnType();
			HttpMethod method = properties.getHttpMethod();
			String url = properties.getUrlExpression().getValue(message, String.class);
			Object body = null;
			if (properties.getBody() != null) {
				body = properties.getBody();
			}
			else if (properties.getBodyExpression() != null) {
				body = properties.getBodyExpression().getValue(message);
			}
			URI uri = new URI(url);
			RequestEntity<?> request = new RequestEntity<Object>(body, headers, method, uri);
			ResponseEntity<?> response = restTemplate.exchange(request, responseType);
			return response.getBody();
		}
		catch (Exception e) {
			LOG.warn("Error in HTTP request", e);
			return null;
		}
	}

}
