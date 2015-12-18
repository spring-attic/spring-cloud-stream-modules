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
package org.springframework.cloud.stream.module.tcp.source;

import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.stream.module.tcp.AbstractTcpConnectionFactoryProperties;

/**
 * Properties for the TCP Source.
 *
 * @author Gary Russell
 *
 */
@ConfigurationProperties
public class TcpSourceProperties extends AbstractTcpConnectionFactoryProperties {

	/**
	 * The decoder to use when receiving messages.
	 */
	private Encoding decoder = Encoding.CRLF;

	/**
	 * The buffer size used when decoding messages; larger messages will be rejected.
	 */
	private int bufferSize = 2048;

	@NotNull
	public Encoding getDecoder() {
		return this.decoder;
	}

	public void setDecoder(Encoding decoder) {
		this.decoder = decoder;
	}

	public int getBufferSize() {
		return bufferSize;
	}

	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}

}
