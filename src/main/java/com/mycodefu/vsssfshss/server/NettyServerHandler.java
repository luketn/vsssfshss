package com.mycodefu.vsssfshss.server;

import static io.netty.handler.codec.http.HttpHeaderNames.HOST;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;

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

        callback.serverConnectionClosed(ctx.channel().id(), messageSender);
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
        void serverConnectionOpened(ChannelId id, MessageSender messageSender,
                String remoteAddress);

        void serverConnectionMessage(ChannelId id, MessageSender messageSender,
                String sourceIpAddress, ByteBuf byteBuf);

        void serverConnectionClosed(ChannelId id, MessageSender messageSender);

        HttpMessage serverHttpMessage(ChannelId id, String ip, String path,
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

                    callback.serverConnectionOpened(channelHandlerContext.channel().id(),
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
                    callback.serverHttpMessage(channelHandlerContext.channel().id(), ip, uri.path(),
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
        callback.serverConnectionMessage(channelHandlerContext.channel().id(), messageSender, ip,
                msg.content());
    }

    private String getWebSocketLocation(FullHttpRequest req) {
        String location = req.headers().get(HOST);
        return "ws://" + location;
    }

}
