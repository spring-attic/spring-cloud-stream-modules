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

package org.springframework.cloud.stream.module.firehose.netty;

import static io.netty.handler.codec.http.HttpHeaderNames.*;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import org.cloudfoundry.dropsonde.events.EventFactory;

/**
 * @author Vinicius Carvalho
 */
public class WebSocketHandler extends SimpleChannelInboundHandler<Object> {

    private static final String WEBSOCKET_PATH = "/firehose/firehose-a";
    private WebSocketServerHandshaker handshaker;
    private ExecutorService pool = Executors.newFixedThreadPool(4);
    private BlockingQueue<EventFactory.Envelope> messages = new ArrayBlockingQueue(10);

    private static String getWebSocketLocation(FullHttpRequest req) {

        String location = req.headers().get(HOST) + WEBSOCKET_PATH;

        return "ws://" + location;
    }

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            handleHttpRequest(ctx, (FullHttpRequest) msg);
        } else if (msg instanceof WebSocketFrame) {
            handleWebSocketFrame(ctx, (WebSocketFrame) msg);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    private void handleHttpRequest(final ChannelHandlerContext ctx, FullHttpRequest req) {

        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                getWebSocketLocation(req), null, true);
        handshaker = wsFactory.newHandshaker(req);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        } else {
            handshaker.handshake(ctx.channel(), req).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    Worker worker = new Worker(ctx);
                    pool.submit(worker);
                }
            });
        }
    }

    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {

        if (frame instanceof CloseWebSocketFrame) {
            System.out.println("Ignoring close frame");
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            return;
        }
        if (frame instanceof PingWebSocketFrame) {
            ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
            return;
        }


    }

    public void addMessage(EventFactory.Envelope envelope) {
        messages.offer(envelope);
    }

    class Worker implements Runnable {
        private ChannelHandlerContext ctx;

        public Worker(ChannelHandlerContext ctx) {
            this.ctx = ctx;
        }

        @Override
        public void run() {

            try {
                EventFactory.Envelope envelope = null;
                while ((envelope = WebSocketHandler.this.messages.take()) != null) {
                    ByteBuf buffer = ctx.alloc().buffer();
                    buffer.writeBytes(envelope.toByteArray());
                    ctx.channel().writeAndFlush(new BinaryWebSocketFrame(buffer));
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
