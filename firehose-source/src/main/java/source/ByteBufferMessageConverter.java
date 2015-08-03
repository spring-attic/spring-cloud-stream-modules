/*
 *
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

package source;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.converter.AbstractMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;

import java.nio.ByteBuffer;

/**
 * @author Vinicius Carvalho
 */
@Component
public class ByteBufferMessageConverter extends AbstractMessageConverter {

    protected ByteBufferMessageConverter() {
        super(MimeTypeUtils.APPLICATION_OCTET_STREAM);
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return ByteBuffer.class.isAssignableFrom(clazz);
    }

    @Override
    public Object convertFromInternal(Message<?> message, Class<?> aClass) {
        ByteBuffer buffer = (ByteBuffer) message.getPayload();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes, 0, bytes.length);
        return bytes;
    }

    @Override
    public Object convertToInternal(Object payload, MessageHeaders messageHeaders) {
        return payload;
    }
}
