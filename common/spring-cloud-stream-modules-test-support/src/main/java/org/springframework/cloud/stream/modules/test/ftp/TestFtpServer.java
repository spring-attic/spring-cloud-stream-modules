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

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.Authentication;
import org.apache.ftpserver.ftplet.AuthenticationFailedException;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.ConcurrentLoginPermission;
import org.apache.ftpserver.usermanager.impl.TransferRatePermission;
import org.apache.ftpserver.usermanager.impl.WritePermission;
import org.junit.rules.TemporaryFolder;

import org.springframework.integration.test.util.SocketUtils;
import org.springframework.util.Assert;

/**
 * Embedded FTP Server for test cases
 *
 * @author Artem Bilan
 * @author Gary Russell
 * @author David Turanski
 */
public class TestFtpServer {

	protected final int ftpPort = SocketUtils.findAvailableServerSocket();

	private final String rootFolderName;

	protected TemporaryFolder ftpTemporaryFolder;

	protected TemporaryFolder localTemporaryFolder;

	protected volatile File ftpRootFolder;

	protected volatile File sourceFtpDirectory;

	protected volatile File targetFtpDirectory;

	protected volatile File sourceLocalDirectory;

	protected volatile File targetLocalDirectory;

	private volatile FtpServer server;


	public TestFtpServer(String rootFolderName) {
		this.rootFolderName = rootFolderName;
	}

	public int getPort() {
		return this.ftpPort;
	}

	public String getRootFolderName() {
		return this.rootFolderName;
	}

	public File getRootFolder() {
		return this.ftpRootFolder;
	}

	public File getSourceFtpDirectory() {
		return sourceFtpDirectory;
	}

	public File getTargetFtpDirectory() {
		return targetFtpDirectory;
	}

	public File getSourceLocalDirectory() {
		return sourceLocalDirectory;
	}

	public File getTargetLocalDirectory() {
		return targetLocalDirectory;
	}

	public String getTargetLocalDirectoryName() {
		return targetLocalDirectory.getAbsolutePath() + File.separator;
	}

	public TestFtpServer setFtpTemporaryFolder(TemporaryFolder ftpTemporaryFolder) {
		this.ftpTemporaryFolder = ftpTemporaryFolder;
		return this;
	}

	public TestFtpServer setLocalTemporaryFolder(TemporaryFolder localTemporaryFolder) {
		this.localTemporaryFolder = localTemporaryFolder;
		return this;
	}


	public TestFtpServer setFtpRootFolder(File ftpRootFolder) {
		this.ftpRootFolder = ftpRootFolder;
		return this;
	}

	public TestFtpServer setSourceFtpDirectory(File sourceFtpDirectory) {
		this.sourceFtpDirectory = sourceFtpDirectory;
		return this;
	}

	public TestFtpServer setTargetFtpDirectory(File targetFtpDirectory) {
		this.targetFtpDirectory = targetFtpDirectory;
		return this;
	}

	public TestFtpServer setSourceLocalDirectory(File sourceLocalDirectory) {
		this.sourceLocalDirectory = sourceLocalDirectory;
		return this;
	}

	public TestFtpServer setTargetLocalDirectory(File targetLocalDirectory) {
		this.targetLocalDirectory = targetLocalDirectory;
		return this;
	}

	@PostConstruct
	public void before() throws Throwable {
		Assert.notNull(ftpTemporaryFolder, "'ftpTemporaryFolder cannot be null.");
		this.ftpTemporaryFolder.create();
		if (localTemporaryFolder != null) {
			this.localTemporaryFolder.create();
		}

		FtpServerFactory serverFactory = new FtpServerFactory();
		serverFactory.setUserManager(new TestUserManager(this.ftpRootFolder.getAbsolutePath()));

		ListenerFactory factory = new ListenerFactory();
		factory.setPort(ftpPort);
		serverFactory.addListener("default", factory.createListener());

		server = serverFactory.createServer();
		server.start();
	}


	@PreDestroy
	public void after() throws Exception {
		stopServer();
		this.ftpTemporaryFolder.delete();
		if (localTemporaryFolder != null) {
			this.localTemporaryFolder.delete();
		}
	}

	public void stopServer() throws Exception {
		this.server.stop();
	}

	public void recursiveDelete(File file) {
		File[] files = file.listFiles();
		if (files != null) {
			for (File each : files) {
				recursiveDelete(each);
			}
		}
		if (!(file.equals(this.targetFtpDirectory) || file.equals(this.targetLocalDirectory))) {
			file.delete();
		}
	}


	private class TestUserManager implements UserManager {

		private final BaseUser testUser;

		private TestUserManager(String homeDirectory) {
			this.testUser = new BaseUser();
			this.testUser.setAuthorities(Arrays.asList(new ConcurrentLoginPermission(1024, 1024),
					new WritePermission(),
					new TransferRatePermission(1024, 1024)));
			this.testUser.setHomeDirectory(homeDirectory);
			this.testUser.setName("TEST_USER");
		}


		@Override
		public User getUserByName(String s) throws FtpException {
			return this.testUser;
		}

		@Override
		public String[] getAllUserNames() throws FtpException {
			return new String[] { "TEST_USER" };
		}

		@Override
		public void delete(String s) throws FtpException {
		}

		@Override
		public void save(User user) throws FtpException {
		}

		@Override
		public boolean doesExist(String s) throws FtpException {
			return true;
		}

		@Override
		public User authenticate(Authentication authentication) throws AuthenticationFailedException {
			return this.testUser;
		}

		@Override
		public String getAdminName() throws FtpException {
			return "admin";
		}

		@Override
		public boolean isAdmin(String s) throws FtpException {
			return s.equals("admin");
		}

	}

}
