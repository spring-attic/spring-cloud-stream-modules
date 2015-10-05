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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.cloud.stream.annotation.Bindings;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.cloud.stream.modules.test.PropertiesInitializer;
import org.springframework.cloud.stream.modules.test.ftp.TestSftpServer;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.context.ApplicationContext;
import org.springframework.integration.endpoint.SourcePollingChannelAdapter;
import org.springframework.integration.test.util.SocketUtils;
import org.springframework.messaging.Message;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


/**
 * @author David Turanski
 * @author Marius Bogoevici
 * @author Gary Russell
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = SftpSourceApplication.class, initializers = PropertiesInitializer.class)
@DirtiesContext
public class SftpSourceIntegrationTests {

	@Autowired ApplicationContext applicationContext;

	@Autowired SourcePollingChannelAdapter sourcePollingChannelAdapter;

	@Autowired
	private MessageCollector messageCollector;

	@Autowired
	private SftpSourceProperties config;

	private static TestSftpServer sftpServer;

	@BeforeClass
	public static void configureSftpServer() throws Throwable {

		sftpServer = new TestSftpServer("sftpTest", SocketUtils.findAvailableServerSocket());
		sftpServer.setFtpTemporaryFolder(
				new TemporaryFolder() {
					@Override
					public void create() throws IOException {
						super.create();
						File rootFolder = this.newFolder(sftpServer.getRootFolderName());
						File sourceFtpDirectory = new File(rootFolder, "sftpSource");
						sourceFtpDirectory.mkdir();

						File file = new File(sourceFtpDirectory, "sftpSource1.txt");
						file.createNewFile();
						FileOutputStream fos = new FileOutputStream(file);
						fos.write("source1".getBytes());
						fos.close();
						file = new File(sourceFtpDirectory, "sftpSource2.txt");
						file.createNewFile();
						fos = new FileOutputStream(file);
						fos.write("source2".getBytes());
						fos.close();

						File targetFtpDirectory = new File(rootFolder, "sftpTarget");
						targetFtpDirectory.mkdir();

						sftpServer
								.setFtpRootFolder(rootFolder)
								.setSourceFtpDirectory(sourceFtpDirectory)
								.setTargetFtpDirectory(targetFtpDirectory);
					}
				});
		sftpServer.setLocalTemporaryFolder(new TemporaryFolder() {

			@Override
			public void create() throws IOException {
				super.create();
				File rootFolder = sftpServer.getRootFolder();
				File sourceLocalDirectory = new File(rootFolder, "localSource");
				sourceLocalDirectory.mkdirs();
				File file = new File(sourceLocalDirectory, "localSource1.txt");
				file.createNewFile();
				file = new File(sourceLocalDirectory, "localSource2.txt");
				file.createNewFile();

				File subSourceLocalDirectory = new File(sourceLocalDirectory, "subLocalSource");
				subSourceLocalDirectory.mkdir();
				file = new File(subSourceLocalDirectory, "subLocalSource1.txt");
				file.createNewFile();

				File targetLocalDirectory = new File(rootFolder, "slocalTarget");
				targetLocalDirectory.mkdir();

				sftpServer
						.setSourceLocalDirectory(sourceLocalDirectory)
						.setTargetLocalDirectory(targetLocalDirectory);
			}
		});

		sftpServer.before();
		Properties properties = new Properties();
		properties.put("remoteDir", sftpServer.getRootFolder().getAbsolutePath() + File.separator
				+ sftpServer.getSourceFtpDirectory().getName());
		properties.put("localDir", sftpServer.getRootFolder().getAbsolutePath() + File.separator
				+ sftpServer.getTargetLocalDirectory().getName());
		properties.put("username", "foo");
		properties.put("password", "foo");
		properties.put("port", sftpServer.getPort());
		properties.put("mode", "ref");
		properties.put("allowUnknownKeys", "true");
		PropertiesInitializer.PROPERTIES = properties;
	}

	@AfterClass
	public static void tearDown() throws Exception {
		sftpServer.after();
	}

	@Autowired
	@Bindings(SftpSource.class)
	Source sftpSource;

	@Test
	public void sourceFilesAsRef() throws InterruptedException {
		for (int i = 1; i <= 2; i++) {
			@SuppressWarnings("unchecked")
			Message<File> received = (Message<File>) messageCollector.forChannel(sftpSource.output()).poll(10,
					TimeUnit.SECONDS);
			assertThat(received.getPayload(), equalTo(new File(config.getLocalDir() + "/sftpSource" + i + ".txt")));
		}
	}

}

