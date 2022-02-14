package com.mycodefu.vsssfshss;

import com.mycodefu.vsssfshss.http.HttpResourceManager;
import com.mycodefu.vsssfshss.names.NameGenerator;
import com.mycodefu.vsssfshss.server.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Entrypoint {
    public static void main(String[] args) {
        NettyServer server =
                new NettyServer(8080, new NettyServerHandler.ServerConnectionCallback() {

                    @Override
                    public void serverConnectionOpened(Channel channel, MessageSender messageSender,
                                                       String remoteAddress) {

                        String name = NameGenerator.generateName();
                        ConnectionAttributes.set(channel, "name", name);

                        String message = "Welcome %s!".formatted(name);
                        messageSender.sendMessage(channel, Unpooled.wrappedBuffer(message.getBytes(StandardCharsets.UTF_8)));
                    }

                    @Override
                    public void serverConnectionMessage(Channel channel, MessageSender messageSender,
                            String sourceIpAddress, ByteBuf byteBuf) {

                        byte[] bytes = new byte[byteBuf.capacity()];
                        byteBuf.getBytes(0, bytes);
                        String message = new String(bytes);
                        System.out.println(message);

                        String name = ConnectionAttributes.get(channel, "name");

                        messageSender.sendMessage(channel, Unpooled.wrappedBuffer((name + ": " + message).getBytes(StandardCharsets.UTF_8)));
                    }

                    @Override
                    public void serverConnectionClosed(Channel channel, MessageSender messageSender) {

                    }

                    @Override
                    public HttpMessage serverHttpMessage(Channel channel, String ip, String path,
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
