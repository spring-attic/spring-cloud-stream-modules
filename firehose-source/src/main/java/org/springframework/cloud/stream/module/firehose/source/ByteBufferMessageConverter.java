/*
 *  Copyright 2015 original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.springframework.cloud.stream.module.firehose.source;

import java.nio.ByteBuffer;

import com.google.protobuf.InvalidProtocolBufferException;
import org.cloudfoundry.dropsonde.events.EventFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.xd.tuple.Tuple;
import org.springframework.xd.tuple.TupleToJsonStringConverter;

/**
 * Converts from Protocol Buffers binary messages into {@link Tuple}
 *
 * @author Vinicius Carvalho
 */
@Component
public class ByteBufferMessageConverter implements MessageConverter {


	private TupleToJsonStringConverter jsonConverter = new TupleToJsonStringConverter();

	@Autowired
	private FirehoseProperties metadata;

	@Override
	public Object fromMessage(Message<?> message, Class<?> targetClass) {
		ByteBuffer buffer = (ByteBuffer) message.getPayload();
		byte[] bytes = new byte[buffer.remaining()];
		buffer.get(bytes, 0, bytes.length);
		try {
			EventFactory.Envelope envelope = EventFactory.Envelope.parseFrom(bytes);
			Tuple result = TupleFactory.createTuple(envelope);
			return (metadata.isOutputJson()) ? jsonConverter.convert(result) : result;
		}
		catch (InvalidProtocolBufferException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Message<?> toMessage(Object payload, MessageHeaders headers) {
		throw new UnsupportedOperationException("Not supported by this converter");
	}
}
