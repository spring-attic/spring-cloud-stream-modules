/*
 * Copyright 2014-2016 the original author or authors.
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

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.trace.TraceRepository;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

/**
 * Simple websocket sink that publishes messages to all attached netty channels.
 * Optionally pushes messages to a {@link TraceRepository} to access the last <tt>n</tt> messages
 * sent.
 *
 * @author Oliver Moser
 */
@EnableBinding(Sink.class)
public class WebsocketSink {

	private static final Log logger = LogFactory.getLog(WebsocketSink.class);

	@Value("${endpoints.websocketsinktrace.enabled:false}")
	private boolean traceEndpointEnabled;

	private final TraceRepository websocketTraceRepository;

	public WebsocketSink(TraceRepository websocketTraceRepository) {
		this.websocketTraceRepository = websocketTraceRepository;
	}

	@ServiceActivator(inputChannel = Sink.INPUT)
	public void websocketSink(Message<?> message) {
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("Handling message: %s", message));
		}

		SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.wrap(message);
		headers.setMessageTypeIfNotSet(SimpMessageType.MESSAGE);
		String messagePayload = message.getPayload().toString();
		for (Channel channel : WebsocketSinkServer.channels) {
			if (logger.isTraceEnabled()) {
				logger.trace(String.format("Writing message %s to channel %s", messagePayload, channel.localAddress()));
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
