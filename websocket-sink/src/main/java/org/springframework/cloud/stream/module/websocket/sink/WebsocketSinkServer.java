/*
 * Copyright 2014-15 the original author or authors.
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
 *
 */

package org.springframework.cloud.stream.module.websocket.sink;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * bootstraps a Netty server using the {@link WebsocketSinkServerInitializer}. Also adds
 * a {@link LoggingHandler} and uses the <tt>websocketLoglevel</tt>
 * from {@link WebsocketSinkProperties#websocketLoglevel}
 *
 * @author Oliver Moser
 */
@Slf4j
public class WebsocketSinkServer {

	static final List<Channel> channels = Collections.synchronizedList(new ArrayList<Channel>());

	@Autowired
	WebsocketSinkProperties properties;

	@Autowired
	WebsocketSinkServerInitializer initializer;

	private EventLoopGroup bossGroup;

	private EventLoopGroup workerGroup;

	@PostConstruct
	public void init() {
		bossGroup = new NioEventLoopGroup(properties.getThreads());
		workerGroup = new NioEventLoopGroup();
	}

	@PreDestroy
	public void shutdown() {
		bossGroup.shutdownGracefully();
		workerGroup.shutdownGracefully();
	}

	public void run() throws InterruptedException {
		new ServerBootstrap().group(bossGroup, workerGroup)
			.channel(NioServerSocketChannel.class)
			.handler(new LoggingHandler(nettyLogLevel()))
			.childHandler(initializer)
			.bind(properties.getWebsocketPort())
			.sync()
			.channel();

		dumpProperties();
	}

	private void dumpProperties() {
		log.info("███████████████████████████████████████████████████████████");
		log.info("                >> websocket-sink config <<                ");
		log.info("");
		log.info("websocketPort:     {}", properties.getWebsocketPort());
		log.info("ssl:               {}", properties.isSsl());
		log.info("websocketPath:     {}", properties.getWebsocketPath());
		log.info("websocketLoglevel: {}", properties.getWebsocketLoglevel());
		log.info("threads:           {}", properties.getThreads());
		log.info("");
		log.info("████████████████████████████████████████████████████████████");
	}

	//
	// HELPERS
	//
	private LogLevel nettyLogLevel() {
		return LogLevel.valueOf(properties.getWebsocketLoglevel().toUpperCase());
	}


}
