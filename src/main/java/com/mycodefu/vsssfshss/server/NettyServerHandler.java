package com.mycodefu.vsssfshss.server;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.netty.handler.codec.http.HttpHeaderNames.HOST;

public class NettyServerHandler extends SimpleChannelInboundHandler<Object> {
    private final ServerConnectionCallback callback;
    private final MessageSender messageSender;

    public NettyServerHandler(ServerConnectionCallback callback, MessageSender messageSender) {
        this.callback = callback;
        this.messageSender = messageSender;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);

        callback.serverConnectionClosed(ctx.channel(), messageSender);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            handleHttpRequest(ctx, (FullHttpRequest) msg);
        } else if (msg instanceof WebSocketFrame) {
            handleWebSocketRequest(ctx, (WebSocketFrame) msg);
        } else {
            throw new IllegalArgumentException("Not HTTP or web socket");
        }
    }

    public interface ServerConnectionCallback {
        void serverConnectionOpened(Channel channel, MessageSender messageSender,
                                    String remoteAddress);

        void serverConnectionMessage(Channel channel, MessageSender messageSender,
                String sourceIpAddress, String message);

        void serverConnectionClosed(Channel channel, MessageSender messageSender);

        HttpMessage serverHttpMessage(Channel channel, String ip, String path,
                Map<String, List<String>> queryParameters, Map<String, String> requestHeaders);
    }

    private void handleHttpRequest(ChannelHandlerContext channelHandlerContext,
            FullHttpRequest msg) {
        String ip = channelHandlerContext.channel().remoteAddress().toString();

        QueryStringDecoder uri = new QueryStringDecoder(msg.uri());
        System.out.printf("%s requested from %s\n", uri.path(), ip);

        if (uri.path().equals("/ws")) {
            WebSocketServerHandshakerFactory wsFactory =
                    new WebSocketServerHandshakerFactory(getWebSocketLocation(msg), null, true);
            WebSocketServerHandshaker handshaker = wsFactory.newHandshaker(msg);
            if (handshaker == null) {
                WebSocketServerHandshakerFactory
                        .sendUnsupportedVersionResponse(channelHandlerContext.channel());
            } else {
                ChannelFuture channelFuture =
                        handshaker.handshake(channelHandlerContext.channel(), msg);
                if (channelFuture.isSuccess()) {
                    System.out.println(channelHandlerContext.channel() + " Connected");

                    callback.serverConnectionOpened(channelHandlerContext.channel(),
                            messageSender,
                            channelHandlerContext.channel().remoteAddress().toString());
                }
            }
        } else {
            Map<String, String> requestHeaders = new HashMap<>();
            for (Map.Entry<String, String> entry : msg.headers()) {
                requestHeaders.put(entry.getKey(), entry.getValue());
            }
            HttpMessage responseMessage =
                    callback.serverHttpMessage(channelHandlerContext.channel(), ip, uri.path(),
                            uri.parameters(), requestHeaders);
            DefaultFullHttpResponse response = new DefaultFullHttpResponse(msg.protocolVersion(),
                    responseMessage.status(), Unpooled.wrappedBuffer(responseMessage.content()));
            responseMessage.headers().forEach((key, value) -> {
                response.headers().add(key, value);
            });
            channelHandlerContext.writeAndFlush(response);
        }
    }

    private void handleWebSocketRequest(ChannelHandlerContext channelHandlerContext,
            WebSocketFrame msg) {
        String ip = channelHandlerContext.channel().remoteAddress().toString();
        byte[] messageBytes = new byte[msg.content().capacity()];
        msg.content().readBytes(messageBytes);
        callback.serverConnectionMessage(channelHandlerContext.channel(), messageSender, ip,
                new String(messageBytes, StandardCharsets.UTF_8));
    }

    private String getWebSocketLocation(FullHttpRequest req) {
        String location = req.headers().get(HOST);
        return "ws://" + location;
    }

}
