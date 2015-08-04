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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableModule;
import org.springframework.cloud.stream.annotation.Source;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.integration.websocket.ClientWebSocketContainer;
import org.springframework.integration.websocket.inbound.WebSocketInboundChannelAdapter;
import org.springframework.messaging.MessageChannel;

import java.nio.ByteBuffer;
import java.util.Collections;

/**
 * @author Vinicius Carvalho
 */
@EnableModule(Source.class)
@Configuration
public class FirehoseSource implements ApplicationListener<ContextRefreshedEvent>{

    @Autowired
    private FirehoseOptionsMetadata metadata;

    @Bean
    public WebSocketInboundChannelAdapter webSocketInboundChannelAdapter(ClientWebSocketContainer webSocketContainer, ByteBufferMessageConverter converter, MessageChannel output) {
        WebSocketInboundChannelAdapter adapter = new WebSocketInboundChannelAdapter(webSocketContainer);
        adapter.setMessageConverters(Collections.singletonList(converter));
        adapter.setOutputChannel(output);
        adapter.setPayloadType(ByteBuffer.class);
        return adapter;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        ApplicationContext ctx = contextRefreshedEvent.getApplicationContext();
        ctx.getBean(ClientWebSocketContainer.class).start();
        ctx.getBean(WebSocketInboundChannelAdapter.class).start();
    }
}
