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

/**
 * @author David Turanski
 */
public class TestSftpServer extends TestFtpServer {

	private final SshServer server = SshServer.setUpDefaultServer();

	private final int port;

	public TestSftpServer(String root, int port) {
		super(root);
		this.port = port;		
	}
	
	
	@PostConstruct
	@Override
	public void before() throws Throwable {
		this.ftpTemporaryFolder.create();
		this.localTemporaryFolder.create();
		this.ftpRootFolder = ftpTemporaryFolder.getRoot();

		server.setPasswordAuthenticator(new PasswordAuthenticator() {


			@Override
			public boolean authenticate(String username, String password,
					org.apache.sshd.server.session.ServerSession session) {
				return true;
			}
		});
		server.setPort(port);
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
	public void after() {
		super.after();
		File hostkey = new File("hostkey.ser");
		if (hostkey.exists()) {
			hostkey.delete();
		}
	}
}
