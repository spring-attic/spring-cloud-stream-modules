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

import org.springframework.boot.context.properties.ConfigurationProperties;

import io.netty.handler.logging.LogLevel;
import lombok.Data;

/**
 * Command line arguments available for {@link WebsocketSink}.
 *
 * @author Oliver Moser
 */
@Data
@ConfigurationProperties
public class WebsocketSinkProperties {

	public static final String DEFAULT_LOGLEVEL = LogLevel.WARN.toString();

	public static final String DEFAULT_PATH = "/websocket";

	public static final int DEFAULT_THREADS = 1;

	public static final int DEFAULT_PORT = 9292;

	/**
	 * whether or not to create a {@link io.netty.handler.ssl.SslContext}
	 */
	boolean ssl;

	/**
	 * the websocketPort on which the Netty server listens. Default is <tt>9292</tt>
	 */
	int websocketPort = DEFAULT_PORT;

	/**
	 * the number of threads for the Netty {@link io.netty.channel.EventLoopGroup}. Default is <tt>1</tt>
	 */
	int threads = DEFAULT_THREADS;

	/**
	 * the websocketLoglevel for netty channels. Default is <tt>WARN</tt>
	 */
	String websocketLoglevel = DEFAULT_LOGLEVEL;

	/**
	 * the websocketPath on which a WebsocketSink consumer needs to connect. Default is <tt>/websocket</tt>
	 */
	String websocketPath = DEFAULT_PATH;
}
