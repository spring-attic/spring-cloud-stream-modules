/*
 * Copyright 2014-2016 the original author or authors.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.annotation.Autowired;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * Bootstraps a Netty server using the {@link WebsocketSinkServerInitializer}. Also adds
 * a {@link LoggingHandler} and uses the <tt>websocketLoglevel</tt>
 * from {@link WebsocketSinkProperties#websocketLoglevel}.
 *
 * @author Oliver Moser
 * @author Gary Russell
 */
public class WebsocketSinkServer {

	private static final Log logger = LogFactory.getLog(WebsocketSinkServer.class);

	static final List<Channel> channels = Collections.synchronizedList(new ArrayList<Channel>());

	@Autowired
	WebsocketSinkProperties properties;

	@Autowired
	WebsocketSinkServerInitializer initializer;

	private EventLoopGroup bossGroup;

	private EventLoopGroup workerGroup;

	private int port;

	public int getPort() {
		return this.port;
	}

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
		NioServerSocketChannel channel = (NioServerSocketChannel) new ServerBootstrap().group(bossGroup, workerGroup)
			.channel(NioServerSocketChannel.class)
			.handler(new LoggingHandler(nettyLogLevel()))
			.childHandler(initializer)
			.bind(properties.getWebsocketPort())
			.sync()
			.channel();
		this.port = channel.localAddress().getPort();
		dumpProperties();
	}

	private void dumpProperties() {
		logger.info("███████████████████████████████████████████████████████████");
		logger.info("                >> websocket-sink config <<                ");
		logger.info("");
		logger.info(String.format("websocketPort:     %s", this.port));
		logger.info(String.format("ssl:               %s", this.properties.isSsl()));
		logger.info(String.format("websocketPath:     %s", this.properties.getWebsocketPath()));
		logger.info(String.format("websocketLoglevel: %s", this.properties.getWebsocketLoglevel()));
		logger.info(String.format("threads:           %s", this.properties.getThreads()));
		logger.info("");
		logger.info("████████████████████████████████████████████████████████████");
	}

	//
	// HELPERS
	//
	private LogLevel nettyLogLevel() {
		return LogLevel.valueOf(properties.getWebsocketLoglevel().toUpperCase());
	}


}
