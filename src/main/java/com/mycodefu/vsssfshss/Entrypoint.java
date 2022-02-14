package com.mycodefu.vsssfshss;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.mycodefu.vsssfshss.http.HttpResourceManager;
import com.mycodefu.vsssfshss.names.NameGenerator;
import com.mycodefu.vsssfshss.server.HttpMessage;
import com.mycodefu.vsssfshss.server.MessageSender;
import com.mycodefu.vsssfshss.server.NettyServer;
import com.mycodefu.vsssfshss.server.NettyServerHandler;
import io.netty.channel.ChannelId;
import io.netty.handler.codec.http.HttpResponseStatus;

public class Entrypoint {
    public static void main(String[] args) {
        NettyServer server =
                new NettyServer(8080, new NettyServerHandler.ServerConnectionCallback() {

                    @Override
                    public void serverConnectionOpened(ChannelId id, MessageSender messageSender,
                            String remoteAddress) {
                        String message = "Welcome %s!".formatted(NameGenerator.generateName());
                        messageSender.sendMessage(id, message);
                    }

                    @Override
                    public void serverConnectionMessage(ChannelId id, MessageSender messageSender,
                            String sourceIpAddress, String message) {
                        System.out.println(message);

                        messageSender.sendMessage(id, message);
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
