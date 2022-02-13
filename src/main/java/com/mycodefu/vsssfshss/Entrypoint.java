package com.mycodefu.vsssfshss;

import com.mycodefu.vsssfshss.server.MessageSender;
import com.mycodefu.vsssfshss.server.NettyServer;
import com.mycodefu.vsssfshss.server.NettyServerHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelId;

public class Entrypoint {
    public static void main(String[] args) {
        NettyServer server =
                new NettyServer(8080, new NettyServerHandler.ServerConnectionCallback() {

                    @Override
                    public void serverConnectionOpened(ChannelId id, MessageSender messageSender,
                            String remoteAddress) {

                    }

                    @Override
                    public void serverConnectionMessage(ChannelId id, MessageSender messageSender,
                            String sourceIpAddress, ByteBuf byteBuf) {
                        byte[] bytes = new byte[byteBuf.capacity()];
                        byteBuf.getBytes(0, bytes);
                        System.out.println(new String(bytes));

                    }

                    @Override
                    public void serverConnectionClosed(ChannelId id, MessageSender messageSender) {

                    }
                });
        server.listen();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.close();
        }));
    }
}
