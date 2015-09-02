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

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.Bindings;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.cloud.stream.module.MaxMessagesProperties;
import org.springframework.cloud.stream.module.PeriodicTriggerConfiguration;
import org.springframework.cloud.stream.module.file.FileConsumerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlowBuilder;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.SourcePollingChannelAdapterSpec;
import org.springframework.integration.dsl.core.Pollers;
import org.springframework.integration.dsl.ftp.Ftp;
import org.springframework.integration.dsl.support.Consumer;
import org.springframework.integration.file.splitter.FileSplitter;
import org.springframework.integration.file.transformer.FileToByteArrayTransformer;
import org.springframework.integration.ftp.session.DefaultFtpSessionFactory;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.messaging.MessageHeaders;
import org.springframework.scheduling.Trigger;

/**
 * @author David Turanski
 * @author Marius Bogoevici
 */
@EnableBinding(Source.class)
@EnableConfigurationProperties({FtpSourceProperties.class,
		FileConsumerProperties.class, MaxMessagesProperties.class})
@Import({PeriodicTriggerConfiguration.class, FtpSessionFactoryConfiguration.class})
public class FtpSource {

	@Autowired
	private FtpSourceProperties config;

	@Autowired
	private FileConsumerProperties fileConsumerConfig;

	@Autowired
	MaxMessagesProperties maxMessagesConfig;

	@Autowired
	DefaultFtpSessionFactory ftpSessionFactory;

	@Autowired
	Trigger trigger;

	@Autowired
	@Bindings(FtpSource.class)
	Source source;

	@Bean
	public PollerMetadata poller() {
		return Pollers.trigger(trigger).maxMessagesPerPoll(maxMessagesConfig.getMaxMessages()).get();
	}

	@Bean
	public IntegrationFlow ftpInboundFlow() {
		IntegrationFlowBuilder flowBuilder = IntegrationFlows.from(Ftp.inboundAdapter(FtpSource.this.ftpSessionFactory)
				.preserveTimestamp(config.isPreserveTimestamp())
				.remoteDirectory(config.getRemoteDir())
				.patternFilter(config.getFilenamePattern())
				.remoteFileSeparator(config.getRemoteFileSeparator())
				.localDirectory(config.getLocalDir())
				.autoCreateLocalDirectory(config.isAutoCreateLocalDir())
				.temporaryFileSuffix(config.getTmpFileSuffix())
				.deleteRemoteFiles(config.isDeleteRemoteFiles())
				, new Consumer<SourcePollingChannelAdapterSpec>() {


			@Override
			public void accept(SourcePollingChannelAdapterSpec sourcePollingChannelAdapterSpec) {
				sourcePollingChannelAdapterSpec
						.autoStartup(false)
						.poller(poller());
			}
		});

		switch (fileConsumerConfig.getMode()) {
			case contents:
				flowBuilder.enrichHeaders(Collections.<String, Object>singletonMap(MessageHeaders.CONTENT_TYPE,
						"application/octet-stream"))
						.transform(new FileToByteArrayTransformer());
				break;
			case lines:
				flowBuilder.enrichHeaders(Collections.<String, Object>singletonMap(MessageHeaders.CONTENT_TYPE,
						"text/plain"))
						.split(new FileSplitter(true, fileConsumerConfig.getWithMarkers()), null);
			case ref:
				break;
			default:
				throw new IllegalArgumentException(fileConsumerConfig.getMode().name() +
						" is not a supported file reading mode.");
		}
		return flowBuilder.channel(source.output()).get();
	}
}
