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

package org.springframework.cloud.stream.module.file.source;

import java.io.File;

import javax.validation.constraints.AssertTrue;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties for the file source.
 *
 * @author Gary Russell
 */
@ConfigurationProperties
public class FileSourceProperties {

	private static final String DEFAULT_DIR = System.getProperty("java.io.tmpdir") +
			File.separator + "dataflow" + File.separator + "intput";

	/**
	 * The directory to poll for new files.
	 */
	private String directory = DEFAULT_DIR;

	/**
	 * Set to true to include an {@code AcceptOnceFileListFilter}.
	 */
	private boolean preventDuplicates = true;

	/**
	 * A simple ant pattern to match files.
	 */
	private String filenamePattern;

	/**
	 * A regex pattern to match files.
	 */
	private String filenameRegex;

	public String getDirectory() {
		return directory;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}

	public boolean isPreventDuplicates() {
		return preventDuplicates;
	}

	public void setPreventDuplicates(boolean preventDuplicates) {
		this.preventDuplicates = preventDuplicates;
	}

	public String getFilenamePattern() {
		return filenamePattern;
	}

	public void setFilenamePattern(String filenamePattern) {
		this.filenamePattern = filenamePattern;
	}

	public String getFilenameRegex() {
		return filenameRegex;
	}

	public void setFilenameRegex(String filenameRegex) {
		this.filenameRegex = filenameRegex;
	}

	@AssertTrue(message = "filenamePattern and filenameRegex are mutually exclusive")
	public boolean isExclusivePatterns() {
		return !(this.filenamePattern != null && this.filenameRegex != null);
	}

}
