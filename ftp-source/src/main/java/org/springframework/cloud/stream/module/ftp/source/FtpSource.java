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

package org.springframework.cloud.stream.module.ftp.source;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.Bindings;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.cloud.stream.module.MaxMessagesProperties;
import org.springframework.cloud.stream.module.file.FileConsumerProperties;
import org.springframework.cloud.stream.module.file.FileUtils;
import org.springframework.cloud.stream.module.ftp.FtpSessionFactoryConfiguration;
import org.springframework.cloud.stream.module.trigger.TriggerConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlowBuilder;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.SourcePollingChannelAdapterSpec;
import org.springframework.integration.dsl.ftp.Ftp;
import org.springframework.integration.dsl.ftp.FtpInboundChannelAdapterSpec;
import org.springframework.integration.dsl.support.Consumer;
import org.springframework.integration.ftp.filters.FtpRegexPatternFileListFilter;
import org.springframework.integration.ftp.filters.FtpSimplePatternFileListFilter;
import org.springframework.integration.ftp.session.DefaultFtpSessionFactory;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.util.StringUtils;

/**
 * @author David Turanski
 * @author Marius Bogoevici
 * @author Gary Russell
 */
@EnableBinding(Source.class)
@EnableConfigurationProperties({FtpSourceProperties.class,
		FileConsumerProperties.class, MaxMessagesProperties.class})
@Import({TriggerConfiguration.class, FtpSessionFactoryConfiguration.class})
public class FtpSource {

	@Autowired
	private FtpSourceProperties config;

	@Autowired
	private FileConsumerProperties fileConsumerProperties;

	@Autowired
	DefaultFtpSessionFactory ftpSessionFactory;

	@Autowired
	@Qualifier("defaultPoller")
	PollerMetadata defaultPoller;

	@Autowired
	@Bindings(FtpSource.class)
	Source source;

	@Bean
	public IntegrationFlow ftpInboundFlow() {
		FtpInboundChannelAdapterSpec messageSourceBuilder = Ftp.inboundAdapter(FtpSource.this.ftpSessionFactory)
				.preserveTimestamp(config.isPreserveTimestamp())
				.remoteDirectory(config.getRemoteDir())
				.remoteFileSeparator(config.getRemoteFileSeparator())
				.localDirectory(config.getLocalDir())
				.autoCreateLocalDirectory(config.isAutoCreateLocalDir())
				.temporaryFileSuffix(config.getTmpFileSuffix())
				.deleteRemoteFiles(config.isDeleteRemoteFiles());

		if (StringUtils.hasText(this.config.getFilenamePattern())) {
			messageSourceBuilder.filter(new FtpSimplePatternFileListFilter(this.config.getFilenamePattern()));
		}
		else if (this.config.getFilenameRegex() != null) {
			messageSourceBuilder
					.filter(new FtpRegexPatternFileListFilter(this.config.getFilenameRegex()));
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
				.channel(source.output())
				.get();
	}

}
