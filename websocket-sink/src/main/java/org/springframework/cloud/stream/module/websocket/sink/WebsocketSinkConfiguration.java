
/*
 * Copyright 2014-15 the original author or authors.
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
 *
 */

package org.springframework.cloud.stream.module.websocket.sink;

import java.security.cert.CertificateException;

import javax.annotation.PostConstruct;
import javax.net.ssl.SSLException;

import org.springframework.boot.actuate.trace.InMemoryTraceRepository;
import org.springframework.boot.actuate.trace.TraceRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.module.websocket.sink.actuator.WebsocketSinkTraceEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Oliver Moser
 */
@Configuration
@EnableConfigurationProperties(WebsocketSinkProperties.class)
public class WebsocketSinkConfiguration {

	private final TraceRepository websocketTraceRepository = new InMemoryTraceRepository();

	@PostConstruct
	public void init() throws InterruptedException, CertificateException, SSLException {
		server().run();
	}

	@Bean
	public WebsocketSinkServer server() {
		return new WebsocketSinkServer();
	}

	@Bean
	public WebsocketSinkServerInitializer initializer() {
		return new WebsocketSinkServerInitializer(websocketTraceRepository);
	}

	@Bean
	@ConditionalOnProperty(value = "endpoints.websocketsinktrace.enabled", havingValue = "true")
	public WebsocketSinkTraceEndpoint websocketTraceEndpoint() {
		return new WebsocketSinkTraceEndpoint(websocketTraceRepository);
	}

	@Bean
	public WebsocketSink websocketSink() {
		return new WebsocketSink(websocketTraceRepository);
	}




}
