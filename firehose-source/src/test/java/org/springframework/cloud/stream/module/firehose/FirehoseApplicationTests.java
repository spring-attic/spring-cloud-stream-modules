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

package org.springframework.cloud.stream.module.firehose;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.cloud.stream.annotation.Source;
import org.springframework.cloud.stream.module.firehose.netty.NettyWebSocketServer;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.websocket.ClientWebSocketContainer;
import org.springframework.integration.websocket.inbound.WebSocketInboundChannelAdapter;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Vinicius Carvalho
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {FirehoseApplication.class})
@WebIntegrationTest
public class FirehoseApplicationTests {


    @Autowired
    private MessageCollector messageCollector;

    private static NettyWebSocketServer server;

    @BeforeClass
    public static void start(){
        server = new NettyWebSocketServer();
        server.start();
    }


    @AfterClass
    public static void stop(){
        server.stop();
    }

    @Autowired
    @Output(Source.OUTPUT)
    private MessageChannel output;


    @Test
    public void contextLoads() throws Exception{

    }

    @ServiceActivator(inputChannel = Source.OUTPUT)
    public void receive(Message message) {
        System.out.println(message.getPayload());
    }

}