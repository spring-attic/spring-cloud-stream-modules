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

package org.springframework.cloud.stream.module.redis.sink;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;

/**
 * A sink that can be used to insert data into a Redis store.
 *
 * @author Eric Bottard
 * @author Mark Pollack
 * @autor Marius Bogoevici
 */
@EnableBinding(Sink.class)
public class RedisSink {

    @Autowired
    @Qualifier
    private MessageHandler redisSinkMessageHandler;

    @ServiceActivator(inputChannel = Sink.INPUT)
    public void redisSink(Message message) {
        redisSinkMessageHandler.handleMessage(message);
    }


}
