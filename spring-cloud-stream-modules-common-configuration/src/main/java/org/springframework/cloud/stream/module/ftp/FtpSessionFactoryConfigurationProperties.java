/*
 * Copyright 2015 the original author or authors.
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

package org.springframework.cloud.stream.module.ftp;

import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.Range;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author David Turanski
 */
@ConfigurationProperties
public class FtpSessionFactoryConfigurationProperties {

	/**
	 * The host name of the FTP server. Default is 'localhost'. 
	 */
	private String host = "localhost";

	/**
	 * The port of the FTP server. Default is 21.
	 */
	private int port = 21;

	/**
	 * The username to use to connect to the FTP server. 
	 */
	private String username;

	/**
	 * The password to use to connect to the FTP server. 
	 */
	private String password;

	/**
	 * The client mode to use for the FTP session. Default is 0.
	 */
	private int clientMode = 0;

	@NotBlank
	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	@Range(min = 0, max = 65535)
	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getClientMode() { return clientMode; }

	public void setClientMode(int clientMode) { this.clientMode = clientMode; }
}
