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

package org.springframework.cloud.stream.module.dataset.sink;

import org.junit.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.EnvironmentTestUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Thomas Risberg
 */
public class DatasetPropertiesTests {

	@Test
	public void fsUriCanBeCustomized() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(context, "fsUri:hdfs://localhost:8020");
		context.register(Conf.class);
		context.refresh();
		DatasetSinkProperties properties = context.getBean(DatasetSinkProperties.class);
		assertThat(properties.getFsUri(), equalTo("hdfs://localhost:8020"));
	}

	@Test
	public void directoryCanBeCustomized() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(context, "directory:/tmp/test");
		context.register(Conf.class);
		context.refresh();
		DatasetSinkProperties properties = context.getBean(DatasetSinkProperties.class);
		assertThat(properties.getDirectory(), equalTo("/tmp/test"));
	}

	@Test
	public void namespaceCanBeCustomized() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(context, "namespace:test");
		context.register(Conf.class);
		context.refresh();
		DatasetSinkProperties properties = context.getBean(DatasetSinkProperties.class);
		assertThat(properties.getNamespace(), equalTo("test"));
	}

	@Test
	public void batchSizeCanBeCustomized() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(context, "batchSize:1000");
		context.register(Conf.class);
		context.refresh();
		DatasetSinkProperties properties = context.getBean(DatasetSinkProperties.class);
		assertThat(properties.getBatchSize(), equalTo(1000));
	}

	@Test
	public void idleTimeoutCanBeCustomized() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(context, "idleTimeout:3000");
		context.register(Conf.class);
		context.refresh();
		DatasetSinkProperties properties = context.getBean(DatasetSinkProperties.class);
		assertThat(properties.getIdleTimeout(), equalTo(3000L));
	}

	@Test
	public void allowNullValuesCanBeCustomized() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(context, "allowNullValues:true");
		context.register(Conf.class);
		context.refresh();
		DatasetSinkProperties properties = context.getBean(DatasetSinkProperties.class);
		assertThat(properties.isAllowNullValues(), equalTo(true));
	}

	@Test
	public void formatCanBeCustomized() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(context, "format:parquet");
		context.register(Conf.class);
		context.refresh();
		DatasetSinkProperties properties = context.getBean(DatasetSinkProperties.class);
		assertThat(properties.getFormat(), equalTo("parquet"));
	}

	@Test
	public void partitionPathCanBeCustomized() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(context, "partitionPath:year('timestamp')");
		context.register(Conf.class);
		context.refresh();
		DatasetSinkProperties properties = context.getBean(DatasetSinkProperties.class);
		assertThat(properties.getPartitionPath(), equalTo("year('timestamp')"));
	}

	@Test
	public void writerCacheSizeCanBeCustomized() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(context, "writerCacheSize:20");
		context.register(Conf.class);
		context.refresh();
		DatasetSinkProperties properties = context.getBean(DatasetSinkProperties.class);
		assertThat(properties.getWriterCacheSize(), equalTo(20));
	}

	@Test
	public void compressionTypeCanBeCustomized() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(context, "compressionType:bzip2");
		context.register(Conf.class);
		context.refresh();
		DatasetSinkProperties properties = context.getBean(DatasetSinkProperties.class);
		assertThat(properties.getCompressionType(), equalTo("bzip2"));
	}

	@Configuration
	@EnableConfigurationProperties(DatasetSinkProperties.class)
	static class Conf {
	}

}
