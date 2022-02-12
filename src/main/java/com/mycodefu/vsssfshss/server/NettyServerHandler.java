package com.mycodefu.vsssfshss.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;

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
        } else {
            throw new IllegalArgumentException("Not HTTP");
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

        System.out.println("Received HTTP Request from " + ip);
        channelHandlerContext.writeAndFlush(new DefaultFullHttpResponse(msg.getProtocolVersion(),
                HttpResponseStatus.OK,
                PooledByteBufAllocator.DEFAULT.buffer(512).writeBytes("Hello World!".getBytes())));
        channelHandlerContext.close();
    }

}
