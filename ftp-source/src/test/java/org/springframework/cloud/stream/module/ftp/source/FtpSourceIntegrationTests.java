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

package org.springframework.cloud.stream.module.ftp.source;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.cloud.stream.module.ftp.source.FtpSourceApplication;
import org.springframework.cloud.stream.module.ftp.source.FtpSourceProperties;
import org.springframework.cloud.stream.modules.test.PropertiesInitializer;
import org.springframework.cloud.stream.modules.test.file.remote.FtpTestSupport;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.context.ApplicationContext;
import org.springframework.integration.endpoint.SourcePollingChannelAdapter;
import org.springframework.integration.test.util.TestUtils;
import org.springframework.messaging.Message;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


/**
 * @author David Turanski
 * @author Marius Bogoevici
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = FtpSourceApplication.class, initializers = PropertiesInitializer.class)
@DirtiesContext
public class FtpSourceIntegrationTests extends FtpTestSupport {

	@Autowired ApplicationContext applicationContext;

	@Autowired SourcePollingChannelAdapter sourcePollingChannelAdapter;

	@Autowired
	private MessageCollector messageCollector;

	@Autowired
	private FtpSourceProperties config;

	@BeforeClass
	public static void configureSource() throws Throwable {

		Properties properties = new Properties();
		properties.put("remoteDir", "ftpSource");
		properties.put("localDir", localTemporaryFolder.getRoot().getAbsolutePath() + File.separator + "localTarget");
		properties.put("username", "foo");
		properties.put("password", "foo");
		properties.put("filenamePattern", "*");
		properties.put("port", port);
		properties.put("mode", "ref");
		PropertiesInitializer.PROPERTIES = properties;
	}

	@Autowired
	Source ftpSource;

	@Test
	public void sourceFilesAsRef() throws InterruptedException {
		assertEquals("*", TestUtils.getPropertyValue(sourcePollingChannelAdapter, "source.synchronizer.filter.path"));
		for (int i = 1; i <= 2; i++) {
			@SuppressWarnings("unchecked")
			Message<File> received = (Message<File>) messageCollector.forChannel(ftpSource.output()).poll(10,
					TimeUnit.SECONDS);
			assertNotNull(received);
			assertThat(received.getPayload(), equalTo(new File(config.getLocalDir() + "/ftpSource" + i + ".txt")));
		}
	}

}

