/*
 *
 *  * Copyright 2016 the original author or authors.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.springframework.cloud.stream.module.kafka.source;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.fail;

import org.junit.Test;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.EnvironmentTestUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;

/**
 * Tests for {@link KafkaConfigurationProperties}
 *
 * @author Soby Chacko
 */
public class KafkaConfigurationPropertiesTest {

    @Test
    public void testEmptyTopics() throws Exception {
        try {
            AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
            EnvironmentTestUtils.addEnvironment(context, "topics:");
            context.register(Config.class);
            context.refresh();
            fail("BeanCreationException expected");
        }
        catch (Exception e) {
            assertThat(e, instanceOf(BeanCreationException.class));
            assertThat(e.getMessage(),
                    containsString("Either a list of topics (--topics) OR partitions.<topic>=<comma separated partitions> (--partitions.<topic>) must be provided"));
        }
    }

    @Test
    public void testEmptyPartitions() throws Exception {
        try {
            AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
            EnvironmentTestUtils.addEnvironment(context, "topics:", "partitions:");
            context.register(Config.class);
            context.refresh();
            fail("BeanCreationException expected");
        }
        catch (Exception e) {
            assertThat(e, instanceOf(BeanCreationException.class));
            assertThat(e.getMessage(),
                    containsString("Either a list of topics (--topics) OR partitions.<topic>=<comma separated partitions> (--partitions.<topic>) must be provided"));
        }
    }

    @Test
    public void testBothTopicAndPartitionsProvidedAtTheSameTime() throws Exception {
        try {
            AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
            EnvironmentTestUtils.addEnvironment(context, "topics:foo", "partitions.bar:0,1");
            context.register(Config.class);
            context.refresh();
            fail("BeanCreationException expected");
        }
        catch (Exception e) {
            assertThat(e, instanceOf(BeanCreationException.class));
            assertThat(e.getMessage(),
                    containsString("Either a list of topics (--topics) OR partitions.<topic>=<comma separated partitions> (--partitions.<topic>) must be provided"));
        }
    }

    @Test
    public void testValidTopicScenario() throws Exception {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        EnvironmentTestUtils.addEnvironment(context, "topics:foo");
        context.register(Config.class);
        context.refresh();
        KafkaConfigurationProperties kafkaConfigurationProperties =
                context.getBean(KafkaConfigurationProperties.class);
        assertThat(kafkaConfigurationProperties.getTopics()[0], containsString("foo"));
    }

    @Test
    public void testValidPartitionsScenario() throws Exception {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        EnvironmentTestUtils.addEnvironment(context, "partitions.bar:0,1");
        context.register(Config.class);
        context.refresh();
        KafkaConfigurationProperties kafkaConfigurationProperties =
                context.getBean(KafkaConfigurationProperties.class);
        assertThat(kafkaConfigurationProperties.getPartitions().get("bar"), is("0,1"));
    }


    @Configuration
    @EnableConfigurationProperties(KafkaConfigurationProperties.class)
    static class Config {

    }
}
