package com.mycodefu.vsssfshss.server;

import io.netty.channel.Channel;

public interface MessageSender {
    void sendMessage(Channel channel, String message);
    void broadcast(String message, Channel... excludeChannels);
}
