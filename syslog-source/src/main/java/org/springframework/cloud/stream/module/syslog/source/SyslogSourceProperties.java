/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.cloud.stream.module.syslog.source;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties for the Syslog Source.
 *
 * @author Gary Russell
 *
 */
@ConfigurationProperties
public class SyslogSourceProperties {

	/**
	 * the buffer size used when decoding messages; larger messages will be rejected.
	 */
	private int bufferSize = 2048;

	/**
	 * tcp or udp
	 */
	private String protocol = "tcp";

	/**
	 * The port to listen on.
	 */
	private int port = 1514;

	/**
	 * whether or not to use NIO (when supporting a large number of connections).
	 */
	private boolean nio = false;

	/**
	 * whether or not to perform a reverse lookup on the incoming socket.
	 */
	private boolean reverseLookup;

	/**
	 * the socket timeout.
	 */
	private int socketTimeout;

	/**
	 * '5424' or '3164' - the syslog format according the the RFC; 3164 is aka 'BSD' format.
	 */
	private String rfc = "3164";

	public int getBufferSize() {
		return bufferSize;
	}

	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public boolean isNio() {
		return nio;
	}

	public void setNio(boolean nio) {
		this.nio = nio;
	}

	public boolean isReverseLookup() {
		return reverseLookup;
	}

	public void setReverseLookup(boolean reverseLookup) {
		this.reverseLookup = reverseLookup;
	}

	public int getSocketTimeout() {
		return socketTimeout;
	}

	public void setSocketTimeout(int socketTimeout) {
		this.socketTimeout = socketTimeout;
	}

	@NotNull
	public String getRfc() {
		return rfc;
	}

	public void setRfc(String rfc) {
		this.rfc = rfc;
	}

	@AssertTrue(message = "rfc must be 5424 or 3164")
	public boolean isSupportedRfc() {
		return "5424".equals(this.rfc) || "3164".equals(this.rfc);
	}

	@AssertTrue(message = "protocl must be 'udp' or 'tcp' or 'both'")
	public boolean isSupportedProtocol() {
		return "tcp".equals(this.protocol) || "udp".equals(this.protocol) || "both".equals(this.protocol);
	}

}
