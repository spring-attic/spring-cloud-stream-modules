/*
 *  Copyright 2015 original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.springframework.cloud.stream.module.firehose.source;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.util.Collections;

import javax.net.ssl.SSLContext;

import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.tomcat.websocket.WsWebSocketContainer;
import org.cloudfoundry.client.lib.CloudCredentials;
import org.cloudfoundry.client.lib.oauth2.OauthClient;
import org.cloudfoundry.client.lib.util.RestUtil;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.integration.websocket.ClientWebSocketContainer;
import org.springframework.integration.websocket.inbound.WebSocketInboundChannelAdapter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

/**
 * @author Vinicius Carvalho
 */
@EnableBinding(Source.class)
@EnableConfigurationProperties(FirehoseProperties.class)
public class FirehoseSource implements InitializingBean {

	@Autowired
	private FirehoseProperties properties;

	@Autowired
	private Source channels;

	@Autowired
	private ByteBufferMessageConverter converter;

	@Bean
	public WebSocketInboundChannelAdapter webSocketInboundChannelAdapter() {
		WebSocketInboundChannelAdapter adapter = new WebSocketInboundChannelAdapter(webSocketContainer());
		adapter.setMessageConverters(Collections.<MessageConverter>singletonList(this.converter));
		adapter.setOutputChannel(channels.output());
		adapter.setPayloadType(ByteBuffer.class);
		return adapter;
	}

	@Bean
	public WebSocketClient wsClient() {
		StandardWebSocketClient wsClient = new StandardWebSocketClient();
		if (properties.isTrustSelfCerts()) {
			SSLContext context = buildSslContext();
			wsClient.setUserProperties(Collections.<String, Object>singletonMap(WsWebSocketContainer.SSL_CONTEXT_PROPERTY, context));
		}
		return wsClient;
	}

	@Bean
	public ClientWebSocketContainer webSocketContainer() {
		ClientWebSocketContainer container = new ClientWebSocketContainer(wsClient(), getDopplerEndpoint());
		HttpHeaders headers = new HttpHeaders();


		if (!StringUtils.isEmpty(properties.getUsername())) {
			OauthClient oauthClient = null;
			try {
				oauthClient = new OauthClient(new URL(properties.getAuthenticationUrl()), new RestUtil().createRestTemplate(null, true));
				oauthClient.init(new CloudCredentials(properties.getUsername(), properties.getPassword()));
				headers.add("Authorization", "bearer " + oauthClient.getToken().getValue());
			}
			catch (MalformedURLException e) {
				throw new IllegalArgumentException("Malformed URL for authentication endpoint. Check your configuration.");
			}

		}
		else {
			headers.add("Authorization", "");
		}
		container.setHeaders(headers);
		container.setOrigin(properties.getOrigin());
		return container;
	}


	private SSLContext buildSslContext() {
		try {
			SSLContextBuilder contextBuilder = new SSLContextBuilder().
					useProtocol("TLS").
					loadTrustMaterial(null, new TrustSelfSignedStrategy());
			return contextBuilder.build();
		}
		catch (GeneralSecurityException e) {
			throw new RuntimeException(e);
		}
	}


	private String getDopplerEndpoint() {
		String url = properties.getDopplerUrl();
		return url + "/firehose/" + properties.getDopplerSubscription();
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		webSocketInboundChannelAdapter().start();
	}
}
