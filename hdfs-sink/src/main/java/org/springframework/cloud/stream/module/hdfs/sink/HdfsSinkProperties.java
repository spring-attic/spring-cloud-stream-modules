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

package org.springframework.cloud.stream.module.hdfs.sink;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.Pattern;

/**
 * Used to configure those Hdfs Sink module options that are related to connecting to Hdfs.
 *
 * @author Thomas Risberg
 */
@SuppressWarnings("unused")
@ConfigurationProperties
public class HdfsSinkProperties {

	/**
	 * URL for HDFS Namenode
	 */
	private String fsUri;

	/**
	 * base path to write files to
	 */
	@Value("/tmp/${spring.application.name:hdfs-sink}")
	private String directory;

	/**
	 * the base filename to use for the created files
	 */
	@Value("${spring.application.name:data}")
	private String fileName;

	/**
	 * the base filename extension to use for the created files
	 */
	private String fileExtension = "txt";

	/**
	 * compression codec alias name (gzip, snappy, bzip2, lzo, or slzo)
	 */
	private String codec = null;

	/**
	 * whether file name should contain uuid
	 */
	private boolean fileUuid = false;

	/**
	 * whether writer is allowed to overwrite files in Hadoop FileSystem
	 */
	private boolean overwrite = false;

	/**
	 * threshold in bytes when file will be automatically rolled over.
	 */
	private int rollover = 1000000000;

	/**
	 * inactivity timeout in ms after which file will be automatically closed
	 */
	private long idleTimeout = 0L;

	/**
	 * timeout in ms, regardless of activity, after which file will be automatically closed
	 */
	private long closeTimeout = 0L;

	/**
	 * prefix for files currently being written
	 */
	private String inUsePrefix;

	/**
	 * suffix for files currently being written
	 */
	private String inUseSuffix;

	/**
	 * maximum number of file open attempts to find a path
	 */
	private int fileOpenAttempts = 10;

	/**
	 * a SpEL expression defining the partition path
	 */
	private String partitionPath;

	public String getFsUri() {
		return fsUri;
	}

	public void setFsUri(String fsUri) {
		this.fsUri = fsUri;
	}

	public String getDirectory() {
		return directory;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileExtension() {
		return fileExtension;
	}

	public void setFileExtension(String fileExtension) {
		this.fileExtension = fileExtension;
	}

	public boolean isFileUuid() {
		return fileUuid;
	}

	public void setFileUuid(boolean fileUuid) {
		this.fileUuid = fileUuid;
	}

	@Pattern(regexp = "(?i)(GZIP|SNAPPY|BZIP2|LZO|SLZO)",
			message = "codec must be one of GZIP, SNAPPY, BZIP2, LZO, or SLZO (case-insensitive)")
	public String getCodec() {
		return codec;
	}

	public void setCodec(String codec) {
		this.codec = codec.toUpperCase();
	}

	public boolean isOverwrite() {
		return overwrite;
	}

	public void setOverwrite(boolean overwrite) {
		this.overwrite = overwrite;
	}

	public int getRollover() {
		return rollover;
	}

	public void setRollover(int rollover) {
		this.rollover = rollover;
	}

	public long getIdleTimeout() {
		return idleTimeout;
	}

	public void setIdleTimeout(long idleTimeout) {
		this.idleTimeout = idleTimeout;
	}

	public long getCloseTimeout() {
		return closeTimeout;
	}

	public void setCloseTimeout(long closeTimeout) {
		this.closeTimeout = closeTimeout;
	}

	public String getInUsePrefix() {
		return inUsePrefix;
	}

	public void setInUsePrefix(String inUsePrefix) {
		this.inUsePrefix = inUsePrefix;
	}

	public String getInUseSuffix() {
		return inUseSuffix;
	}

	public void setInUseSuffix(String inUseSuffix) {
		this.inUseSuffix = inUseSuffix;
	}

	public int getFileOpenAttempts() {
		return fileOpenAttempts;
	}

	public void setFileOpenAttempts(int fileOpenAttempts) {
		this.fileOpenAttempts = fileOpenAttempts;
	}

	public String getPartitionPath() {
		return partitionPath;
	}

	public void setPartitionPath(String partitionPath) {
		this.partitionPath = partitionPath;
	}
}
