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

package org.springframework.cloud.stream.modules.test.file.remote;

import java.io.File;
import java.util.Arrays;

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
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * Provides an embedded FTP Server for test cases.
 *
 * @author Artem Bilan
 * @author Gary Russell
 * @author David Turanski
 */
public class FtpTestSupport extends RemoteFileTestSupport {

	private static volatile FtpServer server;

	public String getTargetLocalDirectoryName() {
		return targetLocalDirectory.getAbsolutePath() + File.separator;
	}

	@BeforeClass
	public static void createServer() throws Exception {
		FtpServerFactory serverFactory = new FtpServerFactory();
		serverFactory.setUserManager(new TestUserManager(remoteTemporaryFolder.getRoot().getAbsolutePath()));

		ListenerFactory factory = new ListenerFactory();
		factory.setPort(port);
		serverFactory.addListener("default", factory.createListener());

		server = serverFactory.createServer();
		server.start();
	}


	@AfterClass
	public static void stopServer() throws Exception {
		server.stop();
	}

	@Override
	protected String prefix() {
		return "ftp";
	}

	private static class TestUserManager implements UserManager {

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
