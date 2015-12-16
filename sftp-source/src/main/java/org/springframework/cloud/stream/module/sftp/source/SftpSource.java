/*
 * Copyright 2015-2016 the original author or authors.
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.cloud.stream.module.MaxMessagesProperties;
import org.springframework.cloud.stream.module.file.FileConsumerProperties;
import org.springframework.cloud.stream.module.file.FileUtils;
import org.springframework.cloud.stream.module.sftp.SftpSessionFactoryConfiguration;
import org.springframework.cloud.stream.module.trigger.TriggerConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlowBuilder;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.SourcePollingChannelAdapterSpec;
import org.springframework.integration.dsl.sftp.Sftp;
import org.springframework.integration.dsl.sftp.SftpInboundChannelAdapterSpec;
import org.springframework.integration.dsl.support.Consumer;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.integration.sftp.filters.SftpRegexPatternFileListFilter;
import org.springframework.integration.sftp.filters.SftpSimplePatternFileListFilter;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.util.StringUtils;

/**
 * @author Gary Russell
 */
@EnableBinding(Source.class)
@EnableConfigurationProperties({SftpSourceProperties.class,
		FileConsumerProperties.class, MaxMessagesProperties.class})
@Import({TriggerConfiguration.class, SftpSessionFactoryConfiguration.class})
public class SftpSource {

	@Autowired
	private SftpSourceProperties properties;

	@Autowired
	private FileConsumerProperties fileConsumerProperties;

	@Autowired
	DefaultSftpSessionFactory sftpSessionFactory;

	@Autowired
	@Qualifier("defaultPoller")
	PollerMetadata defaultPoller;

	@Autowired
	Source source;

	@Bean
	public IntegrationFlow sftpInboundFlow() {
		SftpInboundChannelAdapterSpec messageSourceBuilder = Sftp.inboundAdapter(this.sftpSessionFactory)
				.preserveTimestamp(this.properties.isPreserveTimestamp())
				.remoteDirectory(this.properties.getRemoteDir())
				.remoteFileSeparator(this.properties.getRemoteFileSeparator())
				.localDirectory(this.properties.getLocalDir())
				.autoCreateLocalDirectory(this.properties.isAutoCreateLocalDir())
				.temporaryFileSuffix(this.properties.getTmpFileSuffix())
				.deleteRemoteFiles(this.properties.isDeleteRemoteFiles());

		if (StringUtils.hasText(this.properties.getFilenamePattern())) {
			messageSourceBuilder.filter(new SftpSimplePatternFileListFilter(this.properties.getFilenamePattern()));
		}
		else if (this.properties.getFilenameRegex() != null) {
			messageSourceBuilder
					.filter(new SftpRegexPatternFileListFilter(this.properties.getFilenameRegex()));
		}

		IntegrationFlowBuilder flowBuilder = IntegrationFlows.from(messageSourceBuilder
				, new Consumer<SourcePollingChannelAdapterSpec>() {

			@Override
			public void accept(SourcePollingChannelAdapterSpec sourcePollingChannelAdapterSpec) {
				sourcePollingChannelAdapterSpec
						.poller(defaultPoller);
			}

		});

		return FileUtils.enhanceFlowForReadingMode(flowBuilder, this.fileConsumerProperties)
				.channel(this.source.output())
				.get();
	}

}
