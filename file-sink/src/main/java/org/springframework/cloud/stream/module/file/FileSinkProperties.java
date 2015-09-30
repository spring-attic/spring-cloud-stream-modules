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

package org.springframework.cloud.stream.module.file;

import java.io.File;

import javax.validation.constraints.AssertTrue;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.expression.Expression;
import org.springframework.integration.file.support.FileExistsMode;
import org.springframework.util.StringUtils;

/**
 * Properties for the file sink.
 *
 * @author Mark Fisher
 */
@ConfigurationProperties
public class FileSinkProperties {

	private static final String DEFAULT_DIR =
			File.separator + "tmp" +
			File.separator + "dataflow" +
			File.separator + "output";

	private static final String DEFAULT_NAME = "file-sink";

	/**
	 * whether content should be written as bytes
	 */
	private boolean binary = false;

	/**
	 * charset to use when writing text content
	 */
	private String charset = "UTF-8";

	/**
	 * parent directory of the target file
	 */
	private String dir = DEFAULT_DIR;

	/**
	 * expression to evaluate for the parent directory of the target file
	 */
	private Expression dirExpression;

	/**
	 * the FileExistsMode to use if the target file already exists
	 */
	private FileExistsMode mode = FileExistsMode.APPEND;

	/**
	 * name of the target file
	 */
	private String name = DEFAULT_NAME;

	/**
	 * expression to evaluate for the name of the target file
	 */
	private String nameExpression;

	/**
	 * suffix to append to file name
	 */
	private String suffix = "";

	public boolean isBinary() {
		return binary;
	}

	public void setBinary(boolean binary) {
		this.binary = binary;
	}

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public String getDir() {
		return dir;
	}

	public void setDir(String dir) {
		this.dir = dir;
	}

	public Expression getDirExpression() {
		return dirExpression;
	}

	public void setDirExpression(Expression dirExpression) {
		this.dirExpression = dirExpression;
	}

	public FileExistsMode getMode() {
		return mode;
	}

	public void setMode(FileExistsMode mode) {
		this.mode = mode;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNameExpression() {
		return (nameExpression != null)
				? nameExpression + " + '" + getSuffix() + "'"
				: "'" + name + getSuffix() + "'";
	}

	public void setNameExpression(String nameExpression) {
		this.nameExpression = nameExpression;
	}

	public String getSuffix() {
		String suffixWithDotIfNecessary = "";
		if (StringUtils.hasText(suffix)) {
			suffixWithDotIfNecessary = suffix.startsWith(".") ? suffix : "." + suffix;
		}
		return suffixWithDotIfNecessary;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}

	@AssertTrue(message = "Exactly one of 'name' or 'nameExpression' must be set")
	public boolean isMutuallyExclusiveNameAndNameExpression() {
		return DEFAULT_NAME.equals(name) || nameExpression == null;
	}

	@AssertTrue(message = "Exactly one of 'dir' or 'dirExpression' must be set")
	public boolean isMutuallyExclusiveDirAndDirExpression() {
		return DEFAULT_DIR.equals(dir) || dirExpression == null;
	}
}
