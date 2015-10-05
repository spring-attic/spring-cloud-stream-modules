/*
 * Copyright 2015 the original author or authors.
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

package org.springframework.cloud.stream.module.sftp.source;

import java.io.File;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Gary Russell
 */
@ConfigurationProperties
public class SftpSourceProperties {


	/**
	 * The remote FTP directory, "/" by default.
	 */
	private String remoteDir = "/";

	/**
	 * Set to true to delete remote files after successful transfer, false by default.
	 */
	private boolean deleteRemoteFiles = false;

	/**
	 * The local directory to use for file transfers, System.getProperty("java.io.tmpdir") + "/xd/sftp" by default.
	 */
	private File localDir = new File(System.getProperty("java.io.tmpdir") + "/xd/sftp");

	/**
	 * Set to true to create the local directory if it does not exist, true by default
	 */
	private boolean autoCreateLocalDir = true;

	/**
	 * The suffix to use while the transfer is in progress, ".tmp" by default.
	 */
	private String tmpFileSuffix = ".tmp";

	/**
	 * A filter pattern to match the names of files to transfer.
	 */
	private String pattern;

	/**
	 * A regular expression to match the names of files to transfer.
	 */
	private String regexPattern = null;

	/**
	 * The remote file separator, "/" by default.
	 */
	private String remoteFileSeparator = "/";

	/**
	 * Set to true to preserve the original timestamp.
	 */
	private boolean preserveTimestamp = true;

	public boolean isAutoCreateLocalDir() {
		return this.autoCreateLocalDir;
	}

	public void setAutoCreateLocalDir(boolean autoCreateLocalDir) {
		this.autoCreateLocalDir = autoCreateLocalDir;
	}

	public String getRemoteDir() {
		return this.remoteDir;
	}

	public void setRemoteDir(String remoteDir) {
		this.remoteDir = remoteDir;
	}

	public boolean isDeleteRemoteFiles() {
		return this.deleteRemoteFiles;
	}

	public void setDeleteRemoteFiles(boolean deleteRemoteFiles) {
		this.deleteRemoteFiles = deleteRemoteFiles;
	}

	public File getLocalDir() {
		return this.localDir;
	}

	public void setLocalDir(File localDir) {
		this.localDir = localDir;
	}

	public String getTmpFileSuffix() {
		return this.tmpFileSuffix;
	}

	public void setTmpFileSuffix(String tmpFileSuffix) {
		this.tmpFileSuffix = tmpFileSuffix;
	}

	public String getPattern() {
		return this.pattern;
	}

	public void setPattern(String filenamePattern) {
		this.pattern = filenamePattern;
	}

	public String getRegexPattern() {
		return this.regexPattern;
	}

	public void setRegexPattern(String regexPattern) {
		this.regexPattern = regexPattern;
	}

	public String getRemoteFileSeparator() {
		return this.remoteFileSeparator;
	}

	public void setRemoteFileSeparator(String remoteFileSeparator) {
		this.remoteFileSeparator = remoteFileSeparator;
	}

	public boolean isPreserveTimestamp() {
		return this.preserveTimestamp;
	}

	public void setPreserveTimestamp(boolean preserveTimestamp) {
		this.preserveTimestamp = preserveTimestamp;
	}

}
