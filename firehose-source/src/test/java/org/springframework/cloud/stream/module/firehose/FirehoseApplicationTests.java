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

import org.cloudfoundry.dropsonde.events.EventFactory;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.cloud.stream.annotation.ModuleChannels;
import org.springframework.cloud.stream.annotation.Source;
import org.springframework.cloud.stream.module.firehose.netty.NettyWebSocketServer;
import org.springframework.cloud.stream.module.firehose.netty.WebSocketHandler;
import org.springframework.cloud.stream.module.firehose.source.FirehoseSource;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.messaging.Message;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.TimeUnit;

/**
 * @author Vinicius Carvalho
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {FirehoseApplication.class})
@WebIntegrationTest({"server.port:0", "dopplerUrl:ws://localhost:7777"})
public class FirehoseApplicationTests {


    private static NettyWebSocketServer server;
    private static WebSocketHandler handler;
    @Autowired
    @ModuleChannels(FirehoseSource.class)
    Source firehoseSource;
    @Autowired
    private MessageCollector messageCollector;


    @BeforeClass
    public static void start() {
        handler = new WebSocketHandler();
        server = new NettyWebSocketServer();
        server.start(handler);
    }

    @AfterClass
    public static void stop() {
        server.stop();
    }

    @Test
    public void contextLoads() throws Exception {
        EventFactory.Envelope event = ProtocolGenerator.httpStartStopEvent();
        handler.addMessage(event);
        Message m = messageCollector.forChannel(firehoseSource.output()).poll(10, TimeUnit.SECONDS);
        Assert.assertNotNull(m.getPayload());
    }


}
