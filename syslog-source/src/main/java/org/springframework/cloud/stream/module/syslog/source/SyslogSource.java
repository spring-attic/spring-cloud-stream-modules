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

package org.springframework.cloud.stream.module.syslog.source;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.Bindings;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.serializer.Deserializer;
import org.springframework.integration.ip.config.TcpConnectionFactoryFactoryBean;
import org.springframework.integration.ip.tcp.connection.AbstractServerConnectionFactory;
import org.springframework.integration.ip.tcp.serializer.ByteArrayLfSerializer;
import org.springframework.integration.syslog.DefaultMessageConverter;
import org.springframework.integration.syslog.MessageConverter;
import org.springframework.integration.syslog.RFC5424MessageConverter;
import org.springframework.integration.syslog.inbound.RFC6587SyslogDeserializer;
import org.springframework.integration.syslog.inbound.SyslogReceivingChannelAdapterSupport;
import org.springframework.integration.syslog.inbound.TcpSyslogReceivingChannelAdapter;
import org.springframework.integration.syslog.inbound.UdpSyslogReceivingChannelAdapter;

/**
 * A source module that receives Syslog data.
 *
 * @author Gary Russell
 */
@EnableBinding(Source.class)
@EnableConfigurationProperties(SyslogSourceProperties.class)
public class SyslogSource {

	@Autowired
	@Bindings(SyslogSource.class)
	private Source channels;

	@Autowired
	private SyslogSourceProperties properties;

	@Bean
	@ConditionalOnProperty(name = "protocol", havingValue = "udp")
	public UdpSyslogReceivingChannelAdapter udpAdapter() {
		return createUdpAdapter();
	}

	@Bean
	@ConditionalOnProperty(name = "protocol", havingValue = "both")
	public UdpSyslogReceivingChannelAdapter udpBothAdapter() {
		return createUdpAdapter();
	}

	private UdpSyslogReceivingChannelAdapter createUdpAdapter() {
		UdpSyslogReceivingChannelAdapter adapter = new UdpSyslogReceivingChannelAdapter();
		setAdapterProperties(adapter);
		return adapter;
	}

	@Bean
	@ConditionalOnProperty(name = "protocol", havingValue = "tcp", matchIfMissing = true)
	public TcpSyslogReceivingChannelAdapter tcpAdapter(
			@Qualifier("syslogSourceConnectionFactory") AbstractServerConnectionFactory connectionFactory) {
		return createTcpAdapter(connectionFactory);
	}

	@Bean
	@ConditionalOnProperty(name = "protocol", havingValue = "both")
	public TcpSyslogReceivingChannelAdapter tcpBothAdapter(
			@Qualifier("syslogSourceConnectionFactory") AbstractServerConnectionFactory connectionFactory) {
		return createTcpAdapter(connectionFactory);
	}

	private TcpSyslogReceivingChannelAdapter createTcpAdapter(AbstractServerConnectionFactory connectionFactory) {
		TcpSyslogReceivingChannelAdapter adapter = new TcpSyslogReceivingChannelAdapter();
		adapter.setConnectionFactory(connectionFactory);
		setAdapterProperties(adapter);
		return adapter;
	}

	private void setAdapterProperties(SyslogReceivingChannelAdapterSupport adapter) {
		adapter.setPort(this.properties.getPort());
		adapter.setConverter(syslogConverter());
		adapter.setOutputChannel(this.channels.output());
	}

	@Configuration
	@ConditionalOnProperty(name = "protocol", havingValue = "tcp", matchIfMissing = true)
	protected static class TcpBits {

		@Autowired
		private SyslogSourceProperties properties;

		@Bean
		public TcpConnectionFactoryFactoryBean syslogSourceConnectionFactory(
				@Qualifier("syslogSourceDecoder") Deserializer<?> decoder) throws Exception {
			TcpConnectionFactoryFactoryBean factoryBean = new TcpConnectionFactoryFactoryBean();
			factoryBean.setType("server");
			factoryBean.setPort(this.properties.getPort());
			factoryBean.setUsingNio(this.properties.isNio());
			factoryBean.setLookupHost(this.properties.isReverseLookup());
			factoryBean.setDeserializer(decoder);
			if (this.properties.getSocketTimeout() != null) {
				factoryBean.setSoTimeout(this.properties.getSocketTimeout());
			}
			return factoryBean;
		}

		@Bean
		public Deserializer<?> syslogSourceDecoder() {
			ByteArrayLfSerializer decoder = new ByteArrayLfSerializer();
			decoder.setMaxMessageSize(this.properties.getBufferSize());
			if (this.properties.getRfc().equals("5424")) {
				return new RFC6587SyslogDeserializer(decoder);
			}
			else {
				return decoder;
			}
		}
	}

	@Configuration
	@ConditionalOnProperty(name = "protocol", havingValue = "both")
	protected static class BothBits extends TcpBits {

	}

	@Bean
	public MessageConverter syslogConverter() {
		if (this.properties.getRfc().equals("5424")) {
			return new RFC5424MessageConverter();
		}
		else {
			return new DefaultMessageConverter();
		}
	}

}
