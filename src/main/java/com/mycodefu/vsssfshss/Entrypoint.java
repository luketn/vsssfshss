package com.mycodefu.vsssfshss;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;

import com.mycodefu.vsssfshss.http.HttpResourceManager;
import com.mycodefu.vsssfshss.names.NameGenerator;
import com.mycodefu.vsssfshss.server.HttpMessage;
import com.mycodefu.vsssfshss.server.MessageSender;
import com.mycodefu.vsssfshss.server.NettyServer;
import com.mycodefu.vsssfshss.server.NettyServerHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelId;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.pcap.PcapWriteHandler;

public class Entrypoint {
    public static void main(String[] args) {
        NettyServer server =
                new NettyServer(8080, new NettyServerHandler.ServerConnectionCallback() {

                    @Override
                    public void serverConnectionOpened(ChannelId id, MessageSender messageSender,
                            String remoteAddress) {
                        String message = "Welcome %s!".formatted(NameGenerator.generateName());
                        messageSender.sendMessage(id, Unpooled.wrappedBuffer(message.getBytes(StandardCharsets.UTF_8)));
                    }

                    @Override
                    public void serverConnectionMessage(ChannelId id, MessageSender messageSender,
                            String sourceIpAddress, ByteBuf byteBuf) {

                        byte[] bytes = new byte[byteBuf.capacity()];
                        byteBuf.getBytes(0, bytes);
                        System.out.println(new String(bytes));

                        messageSender.sendMessage(id, byteBuf);
                    }

                    @Override
                    public void serverConnectionClosed(ChannelId id, MessageSender messageSender) {

                    }

                    @Override
                    public HttpMessage serverHttpMessage(ChannelId id, String ip, String path,
                            Map<String, List<String>> queryParameters,
                            Map<String, String> requestHeaders) {
                        byte[] content = HttpResourceManager
                                .getResource(path.equals("/") ? "/index.html" : path);
                        if (content != null) {
                            Map<String, Object> headers = new HashMap<>();
                            headers.put("content-length", content.length);
                            headers.put("content-type", "text/html");
                            return new HttpMessage(HttpResponseStatus.OK, content, headers);
                        } else {
                            content = HttpResourceManager.getResource("/404.html");
                            if (content == null) {
                                throw new RuntimeException("No 404 page found");
                            }
                            Map<String, Object> headers = new HashMap<>();
                            headers.put("content-length", content.length);
                            headers.put("content-type", "text/html");
                            return new HttpMessage(HttpResponseStatus.NOT_FOUND, content, headers);
                        }
                    }
                });
        server.listen();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.close();
        }));
    }
}
