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

package org.springframework.cloud.stream.module.websocket.sink.actuator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.endpoint.AbstractEndpoint;
import org.springframework.boot.actuate.trace.Trace;
import org.springframework.boot.actuate.trace.TraceRepository;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * simple {@link org.springframework.boot.actuate.endpoint.Endpoint} implementation that
 * provides access to Websocket messages last sent/received
 *
 * @author Oliver Moser
 */
@Slf4j
@ConfigurationProperties(prefix = "endpoints.websocketsinktrace", ignoreUnknownFields = true)
public class WebsocketSinkTraceEndpoint extends AbstractEndpoint<List<Trace>> {

	private boolean enabled;

	private final TraceRepository repository;

	public WebsocketSinkTraceEndpoint(TraceRepository repository) {
		super("websocketsinktrace");
		this.repository = repository;
	}

	@PostConstruct
	public void init() {
		log.info("/websocketsinktrace enabled: {}", enabled);
	}

	@Override
	public List<Trace> invoke() {
		return this.repository.findAll();
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}
