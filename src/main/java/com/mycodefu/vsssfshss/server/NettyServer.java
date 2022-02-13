package com.mycodefu.vsssfshss.server;

import java.net.InetSocketAddress;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.address.DynamicAddressConnectHandler;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultEventExecutor;

public class NettyServer implements MessageSender {
    private int initialPort;
    private NioEventLoopGroup bossGroup;
    private NioEventLoopGroup workerGroup;
    private Channel serverSocketChannel;
    private ChannelGroup allChannels;
    private ServerBootstrap bootstrap;

    public NettyServer(int initialPort, NettyServerHandler.ServerConnectionCallback callback) {
        this.initialPort = initialPort;
        this.bossGroup = new NioEventLoopGroup(1);
        this.workerGroup = new NioEventLoopGroup();
        this.allChannels =
                new DefaultChannelGroup("WebSocketServerChannels", new DefaultEventExecutor());

        this.bootstrap = new ServerBootstrap();
        this.bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        allChannels.add(ch);

                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new HttpServerCodec());
                        pipeline.addLast(new HttpObjectAggregator(65536));
                        pipeline.addLast(new NettyServerHandler(callback, NettyServer.this));
                    }
                });
    }

    public int getPort() {
        if (serverSocketChannel != null && serverSocketChannel.isOpen()) {
            return ((InetSocketAddress) serverSocketChannel.localAddress()).getPort();
        } else {
            return 0;
        }
    }

    public void listen() {
        try {
            @SuppressWarnings("unused")
            DynamicAddressConnectHandler dynamicAddressConnectHandler =
                    new DynamicAddressConnectHandler() {};
            serverSocketChannel = this.bootstrap.bind(initialPort).sync().channel();
        } catch (InterruptedException e) {
            throw new RuntimeException("Failed attempting to listen on port " + initialPort
                    + " for web socket connections.", e);
        }
    }

    public void close() {
        try {
            serverSocketChannel.close();
            serverSocketChannel.closeFuture().sync();
        } catch (InterruptedException e) {
            throw new RuntimeException("Failed close gracefully.", e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public void sendMessage(ChannelId id, ByteBuf message) {
        try {
            Channel channel = allChannels.find(id);
            if (channel != null) {
                ByteBuf outboundMessage = message.copy();
                WebSocketFrame frame = new BinaryWebSocketFrame(outboundMessage);
                channel.writeAndFlush(frame);
            }
        } catch (Exception e) {
            System.out.printf("Unable to find the channel %s to send message to.\n",
                    id.asShortText());
            e.printStackTrace();
        }
    }
}
