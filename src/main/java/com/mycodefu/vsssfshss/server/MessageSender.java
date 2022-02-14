package com.mycodefu.vsssfshss.server;

import io.netty.channel.ChannelId;

public interface MessageSender {
    void sendMessage(ChannelId channel, String message);

    void broadcast(String message, ChannelId... excludeChannelIds);
}
