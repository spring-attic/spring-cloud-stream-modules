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

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.boot.actuate.metrics.repository.redis.RedisMetricRepository;
import org.springframework.boot.actuate.metrics.writer.DefaultCounterService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.expression.EvaluationContext;
import org.springframework.integration.context.IntegrationContextUtils;

import javax.annotation.PostConstruct;

/**
 * @author Mark Pollack
 */
@Configuration
@EnableConfigurationProperties(CounterSinkProperties.class)
public class CounterSinkConfiguration {

    @Autowired
	private BeanFactory beanFactory;

    private EvaluationContext evaluationContext;

    @Bean
    public CounterService counterService(RedisMetricRepository redisMetricRepository) {
        return new DefaultCounterService(redisMetricRepository);
    }

    @Bean
    public RedisMetricRepository redisMetricRepository(RedisConnectionFactory redisConnectionFactory) {
        return new RedisMetricRepository(redisConnectionFactory);
    }


    public EvaluationContext evaluationContext() {
        return evaluationContext;
    }

    @PostConstruct
	public void afterPropertiesSet() throws Exception {
		if (this.evaluationContext == null) {
			this.evaluationContext = IntegrationContextUtils.getEvaluationContext(this.beanFactory);
		}
	}
}
