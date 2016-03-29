/*
 * Copyright 2015-2016 the original author or authors.
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

package org.springframework.cloud.stream.module.gemfire;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.springframework.cloud.stream.modules.test.hamcrest.HamcrestMatchers.fieldErrorWithNonEmptyArgument;

import java.net.InetSocketAddress;
import java.util.Locale;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.EnvironmentTestUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.validation.BindException;

/**
 * @author David Turanski
 * @author Gary Russell
 */
public class GemfirePoolPropertiesTests {

	private static Locale vmLocale;

	// This class asserts stuff about localized messages. Force en_US for the duration of the tests
	@BeforeClass
	public static void setupLocale() {
		vmLocale = Locale.getDefault();
		Locale.setDefault(Locale.US);
	}

	@AfterClass
	public static void restoreLocale() {
		Locale.setDefault(vmLocale);
	}

	@Test
	public void connectionAddressesCanBeCustomized() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(context, "hostAddresses:localhost:1234,another.host.com:2345");
		context.register(Conf.class);
		context.refresh();
		GemfirePoolProperties properties = context.getBean(GemfirePoolProperties
				.class);
		InetSocketAddress[] addresses = properties.getHostAddresses();
		assertEquals(2, addresses.length);
		assertThat(addresses[0].getHostName(), equalTo("localhost"));
		assertThat(addresses[0].getPort(), equalTo(1234));
		assertThat(addresses[1].getHostName(), equalTo("another.host.com"));
		assertThat(addresses[1].getPort(), equalTo(2345));
		assertThat(properties.getConnectType(), equalTo(GemfirePoolProperties.ConnectType.locator));
	}

	@Test(expected = Exception.class)
	public void invalidAddressThrowsException() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(context, "hostAddresses:localhost1235");
		context.register(Conf.class);
		context.refresh();
	}

	@Test
	public void emptyAddressThrowsException() {
		try {
			AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
			EnvironmentTestUtils.addEnvironment(context, "hostAddresses:");
			context.register(Conf.class);
			context.refresh();
			fail("should throw exception");
		}
		catch (Exception e) {
			assertThat(e.getCause(), instanceOf(BindException.class));
			BindException bindException = (BindException) e.getCause();
			assertThat(bindException.getAllErrors(), hasItem(fieldErrorWithNonEmptyArgument("hostAddresses")));
		}
	}

	@Test
	public void connectTypeCanBeCustomized() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(context, "connectType:server");
		context.register(Conf.class);
		context.refresh();
		GemfirePoolProperties properties = context.getBean(GemfirePoolProperties
				.class);
		assertThat(properties.getConnectType(), equalTo(GemfirePoolProperties.ConnectType.server));
	}

	@Test
	public void subscriptionsCanBeEnabled() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(context, "subscriptionEnabled:true");
		context.register(Conf.class);
		context.refresh();
		GemfirePoolProperties properties = context.getBean(GemfirePoolProperties
				.class);
		assertThat(properties.isSubscriptionEnabled(), equalTo(true));
	}


	@Configuration
	@EnableConfigurationProperties(GemfirePoolProperties.class)
	@Import(InetSocketAddressConverterConfiguration.class)
	static class Conf {}
}
