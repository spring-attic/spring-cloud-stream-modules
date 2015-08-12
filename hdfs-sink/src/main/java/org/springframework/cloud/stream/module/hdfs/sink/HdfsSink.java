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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.data.hadoop.store.DataStoreWriter;
import org.springframework.integration.annotation.ServiceActivator;

import java.io.IOException;

/**
 * A sink that can be used to insert data into HDFS.
 *
 * @author Thomas Risberg
 */
@EnableBinding(Sink.class)
public class HdfsSink {

	DataStoreWriter<String> dataStoreWriter;

	@Autowired
	public void setDataStoreWriter(DataStoreWriter<String> dataStoreWriter) {
		this.dataStoreWriter = dataStoreWriter;
	}

	@ServiceActivator(inputChannel=Sink.INPUT)
	public void hdfsSink(Object payload) {
		try {
			dataStoreWriter.write(payload.toString());
		} catch (IOException e) {
			throw new IllegalStateException("Error while writing", e);
		}
	}

}
