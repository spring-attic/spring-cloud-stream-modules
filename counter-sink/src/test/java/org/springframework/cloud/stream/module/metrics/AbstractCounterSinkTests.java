/*
 * Copyright 2015 the original author or authors.
 *
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
package org.springframework.cloud.stream.module.metrics;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.repository.MetricRepository;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.cloud.stream.annotation.Bindings;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.cloud.stream.test.junit.redis.RedisTestSupport;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Mark Pollack
 * @author Marius Bogoevici
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = CounterSinkApplication.class)
@IntegrationTest({"server.port:0", "store:redis", "spring.metrics.export.delayMillis:10" })
@DirtiesContext
public abstract class AbstractCounterSinkTests {

    @Rule
    public RedisTestSupport redisAvailableRule = new RedisTestSupport();

    @Autowired
    @Bindings(CounterSink.class)
    protected Sink sink;

    @Autowired
    protected MetricRepository redisMetricRepository;

    @Before
    public void init() {
        redisMetricRepository.reset("counter.simpleCounter");
    }

    @After
    public void clear() {
        redisMetricRepository.reset("counter.simpleCounter");
    }


//    @WebIntegrationTest({"name:simpleCounter"})
//    public static class TestSimpleNameTests extends CounterSinkSimpleNameTests {
//
//        @Test
//        public void testIncrement() throws InterruptedException {
//            assertNotNull(this.sink.input());
//            Message<String> message = MessageBuilder.withPayload("Hi").build();
//            sink.input().send(message);
//            Thread.sleep(20);
//            //Note:  If the name of the counter does not start with 'counter' or 'metric' the 'counter.' prefix is added
//            //       by the DefaultCounterService
//            assertEquals(1, this.redisMetricRepository.findOne("counter.simpleCounter").getValue().longValue());
//        }
//
//    }

//    @WebIntegrationTest({"nameExpression:payload"})
//    public static class TestExpressionNameTests extends CounterSinkSimpleNameTests {
//
//        @Test
//        public void testIncrement() throws InterruptedException {
//            assertNotNull(this.sink.input());
//            Message<String> message = MessageBuilder.withPayload("simpleCounter").build();
//            sink.input().send(message);
//            Thread.sleep(20);
//            //Note:  If the name of the counter does not start with 'counter' or 'metric' the 'counter.' prefix is added
//            //       by the DefaultCounterService
//            assertEquals(1, this.redisMetricRepository.findOne("counter.simpleCounter").getValue().longValue());
//        }
//
//    }

//    @WebIntegrationTest({"spring.application.name:simpleCounter"})
//    public static class TestDefaultNameTests extends CounterSinkSimpleNameTests {
//
//        @Test
//        public void testIncrement() throws InterruptedException {
//            assertNotNull(this.sink.input());
//            Message<String> message = MessageBuilder.withPayload("...").build();
//            sink.input().send(message);
//            Thread.sleep(20);
//            //Note:  If the name of the counter does not start with 'counter' or 'metric' the 'counter.' prefix is added
//            //       by the DefaultCounterService
//            assertEquals(1, this.redisMetricRepository.findOne("counter.simpleCounter").getValue().longValue());
//        }
//
//    }
}
