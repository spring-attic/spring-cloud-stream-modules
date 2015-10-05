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

package org.springframework.cloud.stream.module.sftp;


import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.EnvironmentTestUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;

/**
 * @author David Turanski
 * @author Gary Russell
 */
public class SftpSessionFactoryPropertiesTests {

	@Test
	public void hostCanBeCustomized() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(context, "username:user", "host:myHost");
		context.register(Conf.class);
		context.refresh();
		SftpSessionFactoryProperties properties = context.getBean(SftpSessionFactoryProperties
				.class);
		assertThat(properties.getHost(), equalTo("myHost"));
	}

	@Test
	public void portCanBeCustomized() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(context, "username:user", "host:myHost", "port:12");
		context.register(Conf.class);
		context.refresh();
		SftpSessionFactoryProperties properties = context.getBean(SftpSessionFactoryProperties
				.class);
		assertThat(properties.getPort(), equalTo(12));
	}

	@Test
	public void userCanBeCustomized() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(context, "username:user", "host:myHost");
		context.register(Conf.class);
		context.refresh();
		SftpSessionFactoryProperties properties = context.getBean(SftpSessionFactoryProperties
				.class);
		assertThat(properties.getUsername(), equalTo("user"));
	}

	@Test
	public void passwordCanBeCustomized() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(context, "username:user", "host:myHost", "password:pass");
		context.register(Conf.class);
		context.refresh();
		SftpSessionFactoryProperties properties = context.getBean(SftpSessionFactoryProperties
				.class);
		assertThat(properties.getPassword(), equalTo("pass"));
	}

	@Test
	public void keysEtcCanBeCustomized() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(context, "username:user", "host:myHost", "privateKey:id_rsa",
				"passPhrase:foo");
		context.register(Conf.class);
		context.refresh();
		SftpSessionFactoryProperties properties = context.getBean(SftpSessionFactoryProperties
				.class);
		assertThat(properties.getPrivateKey(), equalTo("id_rsa"));
		assertThat(properties.getPassPhrase(), equalTo("foo"));
	}

	@Test
	public void noPropertiesThrowsMeaningfulException() {
		try {
			AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
			context.register(Conf.class);
			context.refresh();
			fail("should throw exception");
			context.close();
		}
		catch (Exception e) {
			assertThat(e.getCause(), instanceOf(BindException.class));
			BindException bindException = (BindException) e.getCause();
			FieldError fieldError = (FieldError) bindException.getAllErrors().get(0);
			assertThat(fieldError.getArguments()[0].toString(), containsString("user"));
			assertThat(fieldError.getDefaultMessage(), equalTo("may not be empty"));
		}
	}

	@Configuration
	@EnableConfigurationProperties(SftpSessionFactoryProperties.class)
	static class Conf {
	}
}
