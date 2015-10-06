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

package org.springframework.cloud.stream.modules.test.ftp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.sshd.SshServer;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.common.file.FileSystemView;
import org.apache.sshd.common.file.nativefs.NativeFileSystemFactory;
import org.apache.sshd.common.file.nativefs.NativeFileSystemView;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.sftp.SftpSubsystem;
import org.junit.rules.TemporaryFolder;

/**
 * @author David Turanski
 */
public class TestSftpServer extends TestFtpServer {

	private final SshServer server = SshServer.setUpDefaultServer();

	public TestSftpServer(String root, int port) {
		super(root);
		setFtpTemporaryFolder(
				new TemporaryFolder() {
					@Override
					public void create() throws IOException {
						super.create();
						File rootFolder = this.newFolder(getRootFolderName());
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

						setFtpRootFolder(rootFolder);
						setSourceFtpDirectory(sourceFtpDirectory);
						setTargetFtpDirectory(targetFtpDirectory);
					}
				});
		setLocalTemporaryFolder(new TemporaryFolder() {

			@Override
			public void create() throws IOException {
				super.create();
				File rootFolder = getRootFolder();
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

				setSourceLocalDirectory(sourceLocalDirectory);
				setTargetLocalDirectory(targetLocalDirectory);
			}
		});
	}

	@Override
	public void stopServer() throws Exception {
		this.server.stop();
	}

	@PostConstruct
	@Override
	public void before() throws Throwable {
		this.ftpTemporaryFolder.create();
		this.localTemporaryFolder.create();

		server.setPasswordAuthenticator(new PasswordAuthenticator() {


			@Override
			public boolean authenticate(String username, String password,
					org.apache.sshd.server.session.ServerSession session) {
				return true;
			}
		});
		server.setPort(ftpPort);
		server.setKeyPairProvider(new SimpleGeneratorHostKeyProvider("hostkey.ser"));
		SftpSubsystem.Factory sftp = new SftpSubsystem.Factory();
		server.setSubsystemFactories(Arrays.<NamedFactory<Command>>asList(sftp));
		server.setFileSystemFactory(new NativeFileSystemFactory() {

			@Override
			public FileSystemView createFileSystemView(org.apache.sshd.common.Session session) {
				return new NativeFileSystemView(session.getUsername(), false) {
					@Override
					public String getVirtualUserDir() {
						return ftpRootFolder.getAbsolutePath();
					}
				};
			}

		});

		server.start();
	}

	@PreDestroy
	@Override
	public void after() throws Exception {
		super.after();
		File hostkey = new File("hostkey.ser");
		if (hostkey.exists()) {
			hostkey.delete();
		}
	}
}
