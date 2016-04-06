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

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.repository.MetricRepository;
import org.springframework.boot.actuate.metrics.repository.redis.RedisMetricRepository;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.cloud.stream.annotation.Bindings;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.cloud.stream.test.junit.redis.RedisTestSupport;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

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

    protected int sleepTime = 100;

    @Test
    public void testRepository() {
        assertTrue("MetricRepository should be Redis based.", metricRepository instanceof RedisMetricRepository);
    }

    @Autowired
    @Bindings(CounterSink.class)
    protected Sink sink;

    @Autowired
    protected MetricRepository metricRepository;

    @Before
    public void init() {
        metricRepository.reset("counter.simpleCounter");
    }

    @After
    public void clear() {
        metricRepository.reset("counter.simpleCounter");
    }

}
