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
package org.springframework.cloud.task.module.timestamp;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Executes a batch job that logs a timestamp
 * @author Glenn Renfro.
 */
@Configuration
@EnableConfigurationProperties({ TimestampTaskProperties.class })
public class TimestampTask {

	@Autowired
	private JobBuilderFactory jobs;

	@Autowired
	private StepBuilderFactory steps;

	@Autowired
	private TimestampTaskProperties config;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Bean
	protected Tasklet tasklet() {
		return new Tasklet() {
			@Override
			public RepeatStatus execute(StepContribution contribution,
										ChunkContext context) {
				DateFormat dateFormat = new SimpleDateFormat(config.getFormat());
				logger.info(dateFormat.format(new Date()));
				return RepeatStatus.FINISHED;
			}
		};
	}

	@Bean
	public Job job() throws Exception {
		return this.jobs.get("job").start(step1()).build();
	}

	@Bean
	protected Step step1() throws Exception {
		return this.steps.get("step1").tasklet(tasklet()).build();
	}
}
