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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Properties;

import org.hamcrest.Matchers;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.cloud.stream.modules.test.PropertiesInitializer;
import org.springframework.cloud.stream.modules.test.file.remote.FtpTestSupport;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


/**
 * @author David Turanski
 * @author Marius Bogoevici
 * @author Gary Russell
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = FtpSinkApplication.class, initializers = PropertiesInitializer.class)
@DirtiesContext
public class FtpSinkIntegrationTests extends FtpTestSupport {

	@BeforeClass
	public static void configureFtpServer() throws Throwable {

		Properties properties = new Properties();
		properties.put("remoteDir", "ftpTarget");
		properties.put("username", "foo");
		properties.put("password", "foo");
		properties.put("filenamePattern", "*");
		properties.put("port", port);
		properties.put("mode", "FAIL");
		properties.put("filenameExpression", "payload.name.toUpperCase()");
		PropertiesInitializer.PROPERTIES = properties;
	}

	@Autowired
	Sink ftpSink;

	@Test
	public void sendFiles() {
		for (int i = 1; i <= 2; i++) {
			String pathname = "/LOCALSOURCE" + i + ".TXT";
			new File(getTargetRemoteDirectory() + pathname).delete();
			assertFalse(new File(getTargetRemoteDirectory() + pathname).exists());
			this.ftpSink.input().send(new GenericMessage<>(new File(getSourceLocalDirectory() + pathname)));
			assertTrue(new File(getTargetRemoteDirectory() + pathname).exists());
			// verify the upcase on a case-insensitive file system
			File[] files = getTargetRemoteDirectory().listFiles();
			for (File file : files) {
				assertThat(file.getName(), Matchers.startsWith("LOCALSOURCE"));
			}
		}
	}

	@Test
	public void serverRefreshed() { // noop test to test the dirs are refreshed properly
		String pathname = "/LOCALSOURCE1.TXT";
		assertTrue(getTargetRemoteDirectory().exists());
		assertFalse(new File(getTargetRemoteDirectory() + pathname).exists());
	}

}
