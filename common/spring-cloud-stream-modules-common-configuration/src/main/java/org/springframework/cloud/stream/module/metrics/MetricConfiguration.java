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

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.EvaluationContext;
import org.springframework.integration.context.IntegrationContextUtils;

/**
 * Sets up {@link EvaluationContext} for executing SpEL expressions.
 *
 * @author Mark Pollack
 * @author Ilayaperumal Gopinathan
 */
public class MetricConfiguration {

	@Autowired
	private BeanFactory beanFactory;

	private EvaluationContext evaluationContext;

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
