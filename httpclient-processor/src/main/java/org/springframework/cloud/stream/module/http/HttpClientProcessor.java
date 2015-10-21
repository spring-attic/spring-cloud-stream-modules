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

package org.springframework.cloud.stream.module.http;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.integration.annotation.Transformer;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Controller;
import org.springframework.web.client.RestTemplate;

/**
 * A processor module that makes requests to an HTTP resource and emits the 
 * response body as a message payload. This processor can be combined, e.g., 
 * with a time source module to periodically poll results from a HTTP resource.
 *
 * @author Waldemar Hummer
 */
@Controller
@EnableBinding(Processor.class)
public class HttpClientProcessor {

	private static final Logger LOG = LoggerFactory.getLogger(HttpClientProcessor.class);

	@Autowired
	private HttpClientProcessorProperties properties;

	@Autowired
	private RestTemplate restTemplate;

	@Transformer(inputChannel = Processor.INPUT, outputChannel = Processor.OUTPUT)
	public Object transform(Message<?> message) {
		try {
			HttpHeaders headers = new HttpHeaders();
			// TODO: add headers
			HttpMethod method = HttpMethod.valueOf(properties.getHttpMethod());
			URI uri = new URI(properties.getUrl());
			HttpEntity<?> requestEntity = new RequestEntity<Object>(properties.getBody(), headers, method, uri);
			ResponseEntity<String> httpResponse = restTemplate.exchange(
					properties.getUrl(), 
					HttpMethod.valueOf(properties.getHttpMethod()),
					requestEntity,
					String.class);
			return httpResponse.getBody();
		} catch (Exception e) {
			LOG.warn("Error in HTTP request", e);
			return null;
		}
	}

}
