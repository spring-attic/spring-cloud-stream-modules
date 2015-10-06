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

package org.springframework.cloud.stream.module.sftp.source;

import java.util.Collections;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.Bindings;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.cloud.stream.module.MaxMessagesProperties;
import org.springframework.cloud.stream.module.PeriodicTriggerConfiguration;
import org.springframework.cloud.stream.module.file.FileConsumerProperties;
import org.springframework.cloud.stream.module.sftp.SftpSessionFactoryConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlowBuilder;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.SourcePollingChannelAdapterSpec;
import org.springframework.integration.dsl.core.Pollers;
import org.springframework.integration.dsl.sftp.Sftp;
import org.springframework.integration.dsl.sftp.SftpInboundChannelAdapterSpec;
import org.springframework.integration.dsl.support.Consumer;
import org.springframework.integration.file.splitter.FileSplitter;
import org.springframework.integration.file.transformer.FileToByteArrayTransformer;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.integration.sftp.filters.SftpRegexPatternFileListFilter;
import org.springframework.integration.sftp.filters.SftpSimplePatternFileListFilter;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.messaging.MessageHeaders;
import org.springframework.scheduling.Trigger;
import org.springframework.util.StringUtils;

/**
 * @author Gary Russell
 */
@EnableBinding(Source.class)
@EnableConfigurationProperties({SftpSourceProperties.class,
		FileConsumerProperties.class, MaxMessagesProperties.class})
@Import({PeriodicTriggerConfiguration.class, SftpSessionFactoryConfiguration.class})
public class SftpSource {

	@Autowired
	private SftpSourceProperties config;

	@Autowired
	private FileConsumerProperties fileConsumerConfig;

	@Autowired
	MaxMessagesProperties maxMessagesConfig;

	@Autowired
	DefaultSftpSessionFactory sftpSessionFactory;

	@Autowired
	Trigger trigger;

	@Autowired
	@Bindings(SftpSource.class)
	Source source;

	@Bean
	public PollerMetadata poller() {
		return Pollers.trigger(this.trigger).maxMessagesPerPoll(this.maxMessagesConfig.getMaxMessages()).get();
	}

	@Bean
	public IntegrationFlow sftpInboundFlow() {
		SftpInboundChannelAdapterSpec messageSourceBuilder = Sftp.inboundAdapter(this.sftpSessionFactory)
				.preserveTimestamp(this.config.isPreserveTimestamp())
				.remoteDirectory(this.config.getRemoteDir())
				.remoteFileSeparator(this.config.getRemoteFileSeparator())
				.localDirectory(this.config.getLocalDir())
				.autoCreateLocalDirectory(this.config.isAutoCreateLocalDir())
				.temporaryFileSuffix(this.config.getTmpFileSuffix())
				.deleteRemoteFiles(this.config.isDeleteRemoteFiles());

		if (StringUtils.hasText(this.config.getFilenamePattern())) {
			messageSourceBuilder.filter(new SftpSimplePatternFileListFilter(this.config.getFilenamePattern()));
		}
		else if (StringUtils.hasText(this.config.getFilenameRegex())) {
			messageSourceBuilder
					.filter(new SftpRegexPatternFileListFilter(Pattern.compile(this.config.getFilenameRegex())));
		}

		IntegrationFlowBuilder flowBuilder = IntegrationFlows.from(messageSourceBuilder
				, new Consumer<SourcePollingChannelAdapterSpec>() {

			@Override
			public void accept(SourcePollingChannelAdapterSpec sourcePollingChannelAdapterSpec) {
				sourcePollingChannelAdapterSpec
						.autoStartup(false)
						.poller(poller());
			}

		});

		switch (this.fileConsumerConfig.getMode()) {
			case contents:
				flowBuilder.enrichHeaders(Collections.<String, Object>singletonMap(MessageHeaders.CONTENT_TYPE,
						"application/octet-stream"))
						.transform(new FileToByteArrayTransformer());
				break;
			case lines:
				flowBuilder.enrichHeaders(Collections.<String, Object>singletonMap(MessageHeaders.CONTENT_TYPE,
						"text/plain"))
						.split(new FileSplitter(true, this.fileConsumerConfig.getWithMarkers()), null);
			case ref:
				break;
			default:
				throw new IllegalArgumentException(fileConsumerConfig.getMode().name() +
						" is not a supported file reading mode.");
		}
		return flowBuilder.channel(this.source.output()).get();
	}

}
