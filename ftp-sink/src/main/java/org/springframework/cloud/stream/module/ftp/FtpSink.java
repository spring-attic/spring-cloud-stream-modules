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

import org.apache.commons.net.ftp.FTPFile;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.Bindings;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.config.SpelExpressionConverterConfiguration;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.integration.dsl.GenericEndpointSpec;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.ftp.Ftp;
import org.springframework.integration.dsl.ftp.FtpMessageHandlerSpec;
import org.springframework.integration.dsl.support.Consumer;
import org.springframework.integration.file.remote.handler.FileTransferringMessageHandler;
import org.springframework.integration.ftp.session.DefaultFtpSessionFactory;
import org.springframework.integration.ftp.session.FtpRemoteFileTemplate;

/**
 * @author Gary Russell
 */
@EnableBinding(Sink.class)
@EnableConfigurationProperties(FtpSinkProperties.class)
@Import({ FtpSessionFactoryConfiguration.class, SpelExpressionConverterConfiguration.class })
public class FtpSink {

	@Autowired
	private FtpSinkProperties properties;

	@Autowired
	DefaultFtpSessionFactory ftpSessionFactory;

	@Autowired
	@Bindings(FtpSink.class)
	Sink sink;

	@Bean
	public IntegrationFlow ftpInboundFlow() {
		FtpMessageHandlerSpec handlerSpec =
			Ftp.outboundAdapter(new FtpRemoteFileTemplate(this.ftpSessionFactory), this.properties.getMode())
				.remoteDirectory(this.properties.getRemoteDir())
				.remoteFileSeparator(this.properties.getRemoteFileSeparator())
				.autoCreateDirectory(this.properties.isAutoCreateDir())
				.temporaryFileSuffix(this.properties.getTmpFileSuffix());
		if (this.properties.getFilenameExpression() != null) {
			handlerSpec.fileNameExpression(this.properties.getFilenameExpression().getExpressionString());
		}
		return IntegrationFlows.from(Sink.INPUT)
			.handle(handlerSpec,
				new Consumer<GenericEndpointSpec<FileTransferringMessageHandler<FTPFile>>>() {
					@Override
					public void accept(GenericEndpointSpec<FileTransferringMessageHandler<FTPFile>> e) {
						e.autoStartup(false);
					}
				})
			.get();
	}

}
