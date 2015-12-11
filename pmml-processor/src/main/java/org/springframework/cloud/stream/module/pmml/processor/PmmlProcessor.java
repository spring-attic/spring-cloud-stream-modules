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

package org.springframework.cloud.stream.module.pmml.processor;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBException;
import javax.xml.transform.Source;

import org.dmg.pmml.FieldName;
import org.dmg.pmml.Model;
import org.dmg.pmml.PMML;
import org.jpmml.evaluator.Evaluator;
import org.jpmml.evaluator.FieldValue;
import org.jpmml.evaluator.ModelEvaluatorFactory;
import org.jpmml.model.ImportFilter;
import org.jpmml.model.JAXBUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.config.SpelExpressionConverterConfiguration;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.context.annotation.Import;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.context.IntegrationContextUtils;
import org.springframework.integration.support.MutableMessage;
import org.springframework.messaging.Message;
import org.springframework.util.Assert;

/**
 * A processor that evaluates a machine learning model stored in PMML format.
 *
 * @author Eric Bottard
 */
@EnableBinding(Processor.class)
@EnableConfigurationProperties(PmmlProcessorProperties.class)
@Import({CustomConversionServiceRegistrar.class, SpelExpressionConverterConfiguration.class})
public class PmmlProcessor {

	private static final Logger logger = LoggerFactory.getLogger(PmmlProcessor.class);

	private final ModelEvaluatorFactory modelEvaluatorFactory = ModelEvaluatorFactory.newInstance();

	@Autowired
	@Qualifier(IntegrationContextUtils.INTEGRATION_EVALUATION_CONTEXT_BEAN_NAME)
	private EvaluationContext evaluationContext;

	@Autowired
	private PmmlProcessorProperties properties;

	private SpelExpressionParser spelExpressionParser = new SpelExpressionParser();

	private PMML pmml;

	@Autowired
	private BeanFactory beanFactory;

	@PostConstruct
	public void setUp() throws IOException, SAXException, JAXBException {
		try (InputStream is = properties.getModelLocation().getInputStream()) {
			Source transformedSource = ImportFilter.apply(new InputSource(is));
			pmml = JAXBUtil.unmarshalPMML(transformedSource);
			Assert.state(!pmml.getModels().isEmpty(), "The provided PMML file at " + properties.getModelLocation() + " does not contain any model");
		}
	}

	@ServiceActivator(inputChannel = Processor.INPUT, outputChannel = Processor.OUTPUT)
	public Object evaluate(Message<?> input) {
		Model model = selectModel(input);
		Evaluator evaluator = modelEvaluatorFactory.newModelManager(pmml, model);

		Map<FieldName, FieldValue> arguments = new LinkedHashMap<>();

		List<FieldName> activeFields = evaluator.getActiveFields();
		for (FieldName activeField : activeFields) {
			// The raw (ie. user-supplied) value could be any Java primitive value
			Object rawValue = resolveActiveValue(input, activeField.getValue());

			// The raw value is passed through:
			// 1) outlier treatment,
			// 2) missing value treatment,
			// 3) invalid value treatment
			// and 4) type conversion
			FieldValue activeValue = evaluator.prepare(activeField, rawValue);

			arguments.put(activeField, activeValue);
		}

		Map<FieldName, ?> results = evaluator.evaluate(arguments);

		MutableMessage<?> result = new MutableMessage<>(input.getPayload(), input.getHeaders());

		for (Map.Entry<FieldName, ?> entry : results.entrySet()) {
			String fieldName = entry.getKey().getValue();
			Expression expression = properties.getOutputs().get(fieldName);
			if (expression == null) {
				expression = spelExpressionParser.parseExpression("payload." + fieldName);
			}
			logger.debug("Setting result field named {} using SpEL[{} = {}]", fieldName, expression, entry.getValue());
			expression.setValue(evaluationContext, result, entry.getValue());
		}

		return result;

	}

	private Object resolveActiveValue(Message<?> input, String fieldName) {
		Expression expression = properties.getInputs().get(fieldName);
		if (expression == null) {
			// Assume same-name mapping on payload properties
			expression = spelExpressionParser.parseExpression("payload." + fieldName);
		}
		Object result = null;
		try {
			result = expression.getValue(evaluationContext, input);
		}
		catch (SpelEvaluationException e) {
			// The evaluator will get a chance to handle missing values
		}
		logger.debug("Resolving value for input field {} using SpEL[{}], result is {}", fieldName, expression, result);
		return result;
	}

	private Model selectModel(Message<?> input) {
		String modelName = properties.getModelName();
		if (modelName == null && properties.getModelNameExpression() == null) {
			return pmml.getModels().get(0);
		}
		else if (properties.getModelNameExpression() != null) {
			modelName = properties.getModelNameExpression().getValue(evaluationContext, input, String.class);
		}
		for (Model model : pmml.getModels()) {
			if (model.getModelName().equals(modelName)) {
				return model;
			}
		}
		throw new RuntimeException("Unable to use model named '" + modelName + "'");
	}

}
