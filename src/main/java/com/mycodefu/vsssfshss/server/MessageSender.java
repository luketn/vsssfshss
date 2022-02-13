package com.mycodefu.vsssfshss.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelId;

public interface MessageSender {
    void sendMessage(ChannelId channel, ByteBuf message);
}
