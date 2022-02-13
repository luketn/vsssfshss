package com.mycodefu.vsssfshss.server;

import static io.netty.handler.codec.http.HttpHeaderNames.HOST;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;

public class NettyServerHandler extends SimpleChannelInboundHandler<Object> {
    private final ServerConnectionCallback callback;

    public NettyServerHandler(ServerConnectionCallback callback) {
        this.callback = callback;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);

        callback.serverConnectionClosed(ctx.channel().id());
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
        void serverConnectionOpened(ChannelId id, String remoteAddress);

        void serverConnectionMessage(ChannelId id, String sourceIpAddress, ByteBuf byteBuf);

        void serverConnectionClosed(ChannelId id);
    }


    private void handleHttpRequest(ChannelHandlerContext channelHandlerContext,
            FullHttpRequest msg) {
        String ip = channelHandlerContext.channel().remoteAddress().toString();

        QueryStringDecoder uri = new QueryStringDecoder(msg.uri());
        System.out.printf("%s requested from %s\n", uri.path(), ip);

        switch (uri.path()) {
            case "/": {
                byte[] content = """
                        <html>
                        <body>
                        <h1 style="background-color:tomato;">Hello World 2!</h1>
                        </body>
                        </html>
                        """.getBytes();
                DefaultFullHttpResponse response =
                        new DefaultFullHttpResponse(msg.protocolVersion(), HttpResponseStatus.OK,
                                Unpooled.wrappedBuffer(content));
                response.headers().add("Content-Length", content.length);
                response.headers().add("Content-Type", "text/html");
                response.headers().add("Connection", "keep-alive");
                channelHandlerContext.writeAndFlush(response);
                break;
            }
            case "/ws": {
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
                                channelHandlerContext.channel().remoteAddress().toString());
                    }
                }
                break;
            }
            default: {
                byte[] content = """
                        <html>
                        <body>
                        Path not found '%s'.
                        </body>
                        </html>
                        """.formatted(uri.path()).getBytes();
                DefaultFullHttpResponse response =
                        new DefaultFullHttpResponse(msg.protocolVersion(),
                                HttpResponseStatus.NOT_FOUND, Unpooled.wrappedBuffer(content));
                response.headers().add("Content-Length", content.length);
                response.headers().add("Content-Type", "text/html");
                response.headers().add("Connection", "keep-alive");
                channelHandlerContext.writeAndFlush(response);
                break;
            }
        }
    }

    private void handleWebSocketRequest(ChannelHandlerContext channelHandlerContext,
            WebSocketFrame msg) {
        String ip = channelHandlerContext.channel().remoteAddress().toString();
        callback.serverConnectionMessage(channelHandlerContext.channel().id(), ip, msg.content());
    }

    private String getWebSocketLocation(FullHttpRequest req) {
        String location = req.headers().get(HOST);
        return "ws://" + location;
    }

}
