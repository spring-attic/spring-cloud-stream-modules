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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.cloud.stream.annotation.Bindings;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.cloud.stream.modules.test.PropertiesInitializer;
import org.springframework.cloud.stream.modules.test.ftp.TestFtpServer;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.context.ApplicationContext;
import org.springframework.integration.endpoint.SourcePollingChannelAdapter;
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
public class FtpSourceIntegrationTests {

	@Autowired ApplicationContext applicationContext;

	@Autowired SourcePollingChannelAdapter sourcePollingChannelAdapter;

	@Autowired
	private MessageCollector messageCollector;

	@Autowired
	private FtpSourceProperties config;

	@BeforeClass
	public static void configureFtpServer() throws Throwable {

		final TestFtpServer ftpServer = new TestFtpServer("ftpTest");
		ftpServer.setFtpTemporaryFolder(
				new TemporaryFolder() {
					@Override
					public void create() throws IOException {
						super.create();
						File rootFolder = this.newFolder(ftpServer.getRootFolderName());
						File sourceFtpDirectory = new File(rootFolder, "ftpSource");
						sourceFtpDirectory.mkdir();

						File file = new File(sourceFtpDirectory, "ftpSource1.txt");
						file.createNewFile();
						FileOutputStream fos = new FileOutputStream(file);
						fos.write("source1".getBytes());
						fos.close();
						file = new File(sourceFtpDirectory, "ftpSource2.txt");
						file.createNewFile();
						fos = new FileOutputStream(file);
						fos.write("source2".getBytes());
						fos.close();

						File targetFtpDirectory = new File(rootFolder, "ftpTarget");
						targetFtpDirectory.mkdir();

						ftpServer
								.setFtpRootFolder(rootFolder)
								.setSourceFtpDirectory(sourceFtpDirectory)
								.setTargetFtpDirectory(targetFtpDirectory);
					}
				});


		ftpServer.before();
		Properties properties = new Properties();
		properties.put("remoteDir", ftpServer.getSourceFtpDirectory().getName());
		properties.put("username", "foo");
		properties.put("password", "foo");
		properties.put("port", ftpServer.getPort());
		properties.put("mode", "ref");
		PropertiesInitializer.PROPERTIES = properties;
	}

	@Autowired
	@Bindings(FtpSource.class)
	Source ftpSource;

	@Test @Ignore
	public void sourceFilesAsRef() throws InterruptedException {
		for (int i = 1; i <= 2; i++) {
			Message<File> received = (Message<File>) messageCollector.forChannel(ftpSource.output()).poll(1,
					TimeUnit.SECONDS);
			assertThat(received.getPayload(), equalTo(new File(config.getLocalDir() + "/ftpSource" + i + ".txt")));
		}
	}

}

