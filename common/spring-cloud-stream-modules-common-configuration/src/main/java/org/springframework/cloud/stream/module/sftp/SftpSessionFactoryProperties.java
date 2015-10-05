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

package org.springframework.cloud.stream.module.sftp;

import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.Range;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Gary Russell
 */
@ConfigurationProperties
public class SftpSessionFactoryProperties {

	public static final String KNOWNHOSTS_UNDEFINED = "UNDEFINED";

	private String host = "localhost";

	private int port = 22;

	private String username;

	private String password;

	private String privateKey = "";

	private String passPhrase = "";

	private boolean allowUnknownKeys = false;

	private String knownHosts = KNOWNHOSTS_UNDEFINED;

	@NotBlank
	public String getHost() {
		return this.host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	@Range(min = 0, max = 65535)
	public int getPort() {
		return this.port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	@NotBlank
	public String getUsername() {
		return this.username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPrivateKey() {
		return this.privateKey;
	}

	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}

	public String getPassPhrase() {
		return this.passPhrase;
	}

	public void setPassPhrase(String passPhrase) {
		this.passPhrase = passPhrase;
	}

	public boolean isAllowUnknownKeys() {
		return this.allowUnknownKeys;
	}

	public void setAllowUnknownKeys(boolean allowUnknownKeys) {
		this.allowUnknownKeys = allowUnknownKeys;
	}

	public String getKnownHosts() {
		return this.knownHosts;
	}

	public void setKnownHosts(String knownHosts) {
		this.knownHosts = knownHosts;
	}

}
