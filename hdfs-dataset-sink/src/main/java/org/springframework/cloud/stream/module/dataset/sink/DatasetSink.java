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

package org.springframework.cloud.stream.module.dataset.sink;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.data.hadoop.store.dataset.DatasetOperations;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;

import java.util.Collection;
import java.util.Collections;

/**
 * A sink that can be used to insert data into HDFS.
 *
 * @author Thomas Risberg
 */
@EnableBinding(Sink.class)
public class DatasetSink {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private DatasetOperations datasetOperations;

	@Autowired
	public void setDataStoreWriter(DatasetOperations datasetOperations) {
		this.datasetOperations = datasetOperations;
	}

	@ServiceActivator(inputChannel="toSink")
	public void datasetSink(Message<?> message) {
		Object payload = message.getPayload();
		if (payload instanceof Collection<?>) {
			Collection<?> payloads = (Collection<?>) payload;
			if (logger.isDebugEnabled()) {
				logger.debug("Writing a collection of " + payloads.size() +
						" POJOs of type " + payloads.toArray()[0].getClass().getName());
			}
			datasetOperations.write((Collection<?>) message.getPayload());
		} else {
			logger.warn("Expected a collection of POJOs but received " + message.getPayload().getClass().getName());
			datasetOperations.write(Collections.singletonList(message.getPayload()));
		}
	}

}
