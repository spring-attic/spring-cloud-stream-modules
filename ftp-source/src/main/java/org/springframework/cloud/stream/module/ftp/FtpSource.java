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
import org.springframework.cloud.stream.annotation.EnableModule;
import org.springframework.cloud.stream.annotation.ModuleChannels;
import org.springframework.cloud.stream.annotation.Source;
import org.springframework.cloud.stream.module.MaxMessagesConfigurationProperties;
import org.springframework.cloud.stream.module.PeriodicTriggerConfiguration;
import org.springframework.cloud.stream.module.file.FileConsumerConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.integration.channel.AbstractMessageChannel;
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
 */
@EnableModule(Source.class)
@EnableConfigurationProperties({FtpSourceConfigurationProperties.class,
		FileConsumerConfigurationProperties.class, MaxMessagesConfigurationProperties.class})
@Import({PeriodicTriggerConfiguration.class, FtpSessionFactoryConfiguration.class})
public class FtpSource {

	@Autowired
	private FtpSourceConfigurationProperties config;

	@Autowired
	private FileConsumerConfigurationProperties fileConsumerConfig;

	@Autowired
	MaxMessagesConfigurationProperties maxMessagesConfig;

	@Autowired
	DefaultFtpSessionFactory ftpSessionFactory;

	@Autowired
	Trigger trigger;

	@Autowired
	@ModuleChannels(FtpSource.class)
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
				sourcePollingChannelAdapterSpec.autoStartup(config.isAutoStartUp());
				sourcePollingChannelAdapterSpec.poller(poller());
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
