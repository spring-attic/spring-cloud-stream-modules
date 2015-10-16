#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
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

package ${package};

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.${typeAsInSourceProcessorOrSink};
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * ${singleLineDescriptionOfTheModule}.
 */
@EnableBinding(${typeAsInSourceProcessorOrSink}.class)
@EnableConfigurationProperties(${capitalizedNameOfModuleWithoutType}XXX${typeAsInSourceProcessorOrSink}Properties.class)
// The XXX part is needed to workaround a maven archetype bug. You'll want to perform a rename refactoring.
public class ${capitalizedNameOfModuleWithoutType}XXX${typeAsInSourceProcessorOrSink} {

	private static final Logger logger = LoggerFactory.getLogger(${capitalizedNameOfModuleWithoutType}XXX${typeAsInSourceProcessorOrSink}.class);

	@Autowired
	private ${capitalizedNameOfModuleWithoutType}XXX${typeAsInSourceProcessorOrSink}Properties properties;

	@Autowired
	private ${typeAsInSourceProcessorOrSink} channels;

}
