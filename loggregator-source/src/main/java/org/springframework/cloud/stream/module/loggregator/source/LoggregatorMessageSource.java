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
package org.springframework.cloud.stream.module.loggregator.source;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudfoundry.client.lib.ApplicationLogListener;
import org.cloudfoundry.client.lib.CloudFoundryClient;
import org.cloudfoundry.client.lib.domain.ApplicationLog;
import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.ReflectionUtils;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.cloud.stream.module.loggregator.source.LoggregatorMessageSource.LoggregatorHeaders.*;

/**
 * Spring Integration inbound adapter that publishes messages whenever a new
 * Loggregator {@link ApplicationLog} log message is received. The message is
 * converted to a {@link String}.
 *
 * @author <a href="josh@joshlong.com">Josh Long</a>
 */
public class LoggregatorMessageSource extends MessageProducerSupport {

	private final CloudFoundryClient cloudFoundryClient;
	private final String applicationName;

	protected LoggregatorMessageSource(
			String applicationName,
			CloudFoundryClient cloudFoundryClient,
			MessageChannel out) {
		super();
		this.applicationName = applicationName;
		this.cloudFoundryClient = cloudFoundryClient;
		setOutputChannel(out);
	}

	@Override
	protected void doStart() {
		this.cloudFoundryClient.streamLogs(this.applicationName,
				new LoggregatorApplicationLogListener());
	}

	private class LoggregatorApplicationLogListener implements ApplicationLogListener {

		private Log log = LogFactory.getLog(getClass());

		@Override
		public void onMessage(ApplicationLog applicationLog) {
			Map<String, Object> headers = new HashMap<>();
			headers.put(APPLICATION_ID.asHeader(), applicationLog.getAppId());
			headers.put(SOURCE_ID.asHeader(), applicationLog.getSourceId());
			headers.put(MESSAGE_TYPE.asHeader(), applicationLog.getMessageType());
			headers.put(SOURCE_NAME.asHeader(), applicationLog.getSourceName());
			headers.put(TIMESTAMP.asHeader(), applicationLog.getTimestamp());
			sendMessage(MessageBuilder.withPayload(applicationLog.getMessage())
					.copyHeaders(headers)
					.build());
		}

		@Override
		public void onComplete() {
			log.info(String.format("completed streaming logs @ %s", getClass().getName()));
		}

		@Override
		public void onError(Throwable throwable) {
			log.error(String.format("error when streaming logs from %s in %s",
					applicationName, getClass().getName()), throwable);
			ReflectionUtils.rethrowRuntimeException(throwable);
		}
	}

	enum LoggregatorHeaders {

		APPLICATION_ID,
		MESSAGE_TYPE,
		SOURCE_ID,
		SOURCE_NAME,
		TIMESTAMP;

		public String asHeader() {
			return this.toString().toLowerCase();
		}
	}

}
