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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.ftp.session.DefaultFtpSessionFactory;

/**
 * FTP Session factory configuration.
 *
 * @author David Turanski
 * @author Gary Russell
 */
@Configuration
@EnableConfigurationProperties(FtpSessionFactoryProperties.class)
public class FtpSessionFactoryConfiguration {

	@Autowired
	private FtpSessionFactoryProperties config;

	@Bean
	public DefaultFtpSessionFactory ftpSessionFactory() {
		DefaultFtpSessionFactory ftpSessionFactory = new DefaultFtpSessionFactory();
		ftpSessionFactory.setHost(config.getHost());
		ftpSessionFactory.setPort(config.getPort());
		ftpSessionFactory.setUsername(config.getUsername());
		ftpSessionFactory.setPassword(config.getPassword());
		ftpSessionFactory.setClientMode(config.getClientMode().getMode());
		return ftpSessionFactory;
	}

}
