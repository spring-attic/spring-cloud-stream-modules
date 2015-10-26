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

import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.trace.TraceRepository;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Simple websocket sink that publishes messages to all attached netty channels.
 * Optionally pushes messages to a {@link TraceRepository} to access the last <tt>n</tt> messages
 * sent.
 *
 * @author Oliver Moser
 */
@Slf4j
@EnableBinding(Sink.class)
public class WebsocketSink {

	@Value("${endpoints.websocketsinktrace.enabled:false}")
	private boolean traceEndpointEnabled;

	private final TraceRepository websocketTraceRepository;

	public WebsocketSink(TraceRepository websocketTraceRepository) {
		this.websocketTraceRepository = websocketTraceRepository;
	}

	@ServiceActivator(inputChannel = Sink.INPUT)
	public void websocketSink(Message<?> message) {
		if (log.isTraceEnabled()) {
			log.trace("Handling message: {}", message);
		}

		SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.wrap(message);
		headers.setMessageTypeIfNotSet(SimpMessageType.MESSAGE);
		String messagePayload = message.getPayload().toString();
		for (Channel channel : WebsocketSinkServer.channels) {
			if (log.isTraceEnabled()) {
				log.trace("Writing message {} to channel {}", messagePayload, channel.localAddress());
			}

			channel.write(new TextWebSocketFrame(messagePayload));
			channel.flush();
		}

		if (traceEndpointEnabled) {
			addMessageToTraceRepository(message);
		}
	}

	private void addMessageToTraceRepository(Message<?> message) {
		Map<String, Object> trace = new LinkedHashMap<>();
		trace.put("type", "text");
		trace.put("direction", "out");
		trace.put("id", message.getHeaders().getId());
		trace.put("payload", message.getPayload().toString());
		websocketTraceRepository.add(trace);
	}
}
