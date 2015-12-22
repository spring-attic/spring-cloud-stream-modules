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

package org.springframework.cloud.stream.module.splitter;

import javax.validation.constraints.AssertTrue;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.expression.Expression;

/**
 * Configuration properties for the Splitter Processor module.
 *
 * @author Gary Russell
 */
@ConfigurationProperties
public class SplitterProcessorProperties {

	/**
	 * A SpEL expression for splitting payloads.
	 */
	private Expression expression;

	/**
	 * When expression is null, delimiters to use when tokenizing
	 * {@link String} payloads.
	 */
	private String delimiters;

	/**
	 * Set to true or false to use a {@code FileSplitter} (to split
	 * text-based files by line) that includes
	 * (or not) beginning/end of file markers.
	 */
	private Boolean fileMarkers;

	/**
	 * The charset to use when converting bytes in text-based files
	 * to String.
	 */
	private String charset;

	/**
	 * Add correlation/sequence information in headers to facilitate later
	 * aggregation.
	 */
	private boolean applySequence = true;

	public void setExpression(Expression expression) {
		this.expression = expression;
	}

	public Expression getExpression() {
		return expression;
	}

	public String getDelimiters() {
		return delimiters;
	}

	public void setDelimiters(String delimiters) {
		this.delimiters = delimiters;
	}

	public Boolean getFileMarkers() {
		return fileMarkers;
	}

	public void setFileMarkers(Boolean fileMarkers) {
		this.fileMarkers = fileMarkers;
	}

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public boolean isApplySequence() {
		return applySequence;
	}

	public void setApplySequence(boolean applySequence) {
		this.applySequence = applySequence;
	}

	@AssertTrue(message = "'delimiters' is not allowed when an 'expression' is provided")
	public boolean isDelimitersAllowed() {
		return this.expression != null ? this.delimiters == null : true;
	}

	@AssertTrue(message = "File properties are not allowed when an 'expression' or 'delimeters' property is provided")
	public boolean isFilePropsAllowed() {
		return this.expression != null || this.delimiters != null
				?  this.fileMarkers == null && this.charset == null : true;
	}

}
