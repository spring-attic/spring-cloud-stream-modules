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
package org.springframework.cloud.stream.module.mail;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Configuration properties for the Mail Source module.
 *
 * @author Amol
 */
@ConfigurationProperties
public class MailSourceProperties {

	/**
	 * Protocol to be used for recieving mail.
	 */
	private String protocol;

	/**
	 * Username for email account. Usually is email id
	 */
	private String username;

	/**
	 * Password for email account.
	 */
	private String password;

	/**
	 * Host URL for email access
	 */
	private String host;

	/**
	 * Port to be used for email e.g. 993 for imaps
	 */
	private String port;

	/**
	 * Email folder to be scanned for email
	 */
	private String folder;

	/**
	 * Set to true to mark email as read.
	 */
	private boolean markAsRead = false;

	/**
	 * Set to true to delete email after download.
	 */
	private boolean delete = false;

	/**
	 * Comma seperated list of javaMail properties to be used.
	 */
	private String javaMailProperties;

	/**
	 * @return the protocol
	 */
	public String getProtocol() {
		return protocol;
	}

	/**
	 * @param protocol
	 *            the protocol to set
	 */
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param username
	 *            the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password
	 *            the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return the host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * @param host
	 *            the host to set
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * @return the port
	 */
	public String getPort() {
		return port;
	}

	/**
	 * @param port
	 *            the port to set
	 */
	public void setPort(String port) {
		this.port = port;
	}

	/**
	 * @return the folder
	 */
	public String getFolder() {
		return folder;
	}

	/**
	 * @param folder
	 *            the folder to set
	 */
	public void setFolder(String folder) {
		this.folder = folder;
	}

	/**
	 * @return the markAsRead
	 */
	public boolean isMarkAsRead() {
		return markAsRead;
	}

	/**
	 * @param markAsRead
	 *            the markAsRead to set
	 */
	public void setMarkAsRead(boolean markAsRead) {
		this.markAsRead = markAsRead;
	}

	/**
	 * @return the delete
	 */
	public boolean isDelete() {
		return delete;
	}

	/**
	 * @param delete
	 *            the delete to set
	 */
	public void setDelete(boolean delete) {
		this.delete = delete;
	}

	/**
	 * @return the javaMailProperties
	 */
	public String getJavaMailProperties() {
		return javaMailProperties;
	}

	/**
	 * @param javaMailProperties
	 *            the javaMailProperties to set
	 */
	public void setJavaMailProperties(String javaMailProperties) {
		this.javaMailProperties = javaMailProperties;
	}

	protected Map<String, String> parsePropertiesString() {
		Map<String, String> variableMap = new HashMap<String, String>();
		if (StringUtils.hasText(javaMailProperties)) {
			String[] nameValues = javaMailProperties.split(",");
			for (String nameValue : nameValues) {
				String[] tokens = nameValue.split("=");
				Assert.state(tokens.length == 2, "Invalid 'properties' format: " + javaMailProperties);
				String name = tokens[0].trim();
				String value = tokens[1].trim();
				variableMap.put(name, value);
			}
		}
		return variableMap;
	}

}
