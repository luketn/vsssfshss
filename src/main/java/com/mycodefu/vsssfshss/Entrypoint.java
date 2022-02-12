package com.mycodefu.vsssfshss;

import com.mycodefu.vsssfshss.server.NettyServer;
import com.mycodefu.vsssfshss.server.NettyServerHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelId;

public class Entrypoint {
    public static void main(String[] args) {
        NettyServer server =
                new NettyServer(8080, new NettyServerHandler.ServerConnectionCallback() {

                    @Override
                    public void serverConnectionOpened(ChannelId id, String remoteAddress) {

                    }

                    @Override
                    public void serverConnectionMessage(ChannelId id, String sourceIpAddress,
                            ByteBuf byteBuf) {

                    }

                    @Override
                    public void serverConnectionClosed(ChannelId id) {

                    }
                });
        server.listen();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.close();
        }));
    }
}
