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

import org.hibernate.validator.constraints.NotBlank;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

/**
 * @author Vinicius Carvalho
 */
@ConfigurationProperties
public class FirehoseProperties {

	/**
	 * Doppler URL endpoint. Usually wss://doppler.{CF_DOMAIN}.
	 */
	private String dopplerUrl;

	/**
	 * Domain of you CloudFoundry installation. Not required for lattice.
	 */
	private String cfDomain;

	/**
	 * Authentication endpoint of your Cloud Foundry installation. Not required for lattice.
	 */
	private String authenticationUrl;

	/**
	 * Cloud Foundry user that has permission to consume doppler events.
	 */
	private String username;

	/**
	 * User password.
	 */
	private String password;

	/**
	 * Comma separated list of doppler events to consume.
	 * Possible values: HTTP_START, HTTP_STOP, HTTP_START_STOP, LOG_EVENT, COUNTER_EVENT, VALUE_METRIC, CONTAINER_METRIC
	 */
	private String dopplerEvents;

	/**
	 * Name of doppler subscription. Creates a websocket session on doppler server.
	 */
	private String dopplerSubscription = "firehose-a";

	/**
	 * Output a JSON string or POJO.
	 */
	private boolean outputJson = false;

	/**
	 * Trust all certs.
	 */
	private boolean trustSelfCerts = false;

	/**
	 * WebSocket origin.
	 */
	private String origin = "http://localhost";

	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	@NotBlank
	public String getDopplerUrl() {
		return StringUtils.isEmpty(dopplerUrl) ? "wss://doppler." + cfDomain : dopplerUrl;
	}

	public void setDopplerUrl(String dopplerUrl) {
		this.dopplerUrl = dopplerUrl;
	}

	public String getCfDomain() {
		return cfDomain;
	}

	public void setCfDomain(String cfDomain) {
		this.cfDomain = cfDomain;
	}

	public String getAuthenticationUrl() {
		return authenticationUrl;
	}

	public void setAuthenticationUrl(String authenticationUrl) {
		this.authenticationUrl = authenticationUrl;
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

	public String getDopplerEvents() {
		return dopplerEvents;
	}

	public void setDopplerEvents(String dopplerEvents) {
		this.dopplerEvents = dopplerEvents;
	}

	public String getDopplerSubscription() {
		return dopplerSubscription;
	}

	public void setDopplerSubscription(String dopplerSubscription) {
		this.dopplerSubscription = dopplerSubscription;
	}

	public boolean isOutputJson() {
		return outputJson;
	}

	public void setOutputJson(boolean outputJson) {
		this.outputJson = outputJson;
	}

	public boolean isTrustSelfCerts() {
		return trustSelfCerts;
	}

	public void setTrustSelfCerts(boolean trustSelfCerts) {
		this.trustSelfCerts = trustSelfCerts;
	}
}
