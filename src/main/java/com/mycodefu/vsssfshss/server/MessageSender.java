package com.mycodefu.vsssfshss.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelId;

public interface MessageSender {
    void sendMessage(Channel channel, ByteBuf message);
    void broadcast(ByteBuf message, Channel... excludeChannels);
}
