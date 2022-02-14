package com.mycodefu.vsssfshss.server;

import io.netty.channel.Channel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

public class ConnectionAttributes {
    public static String get(Channel channel, String name) {
        Attribute<String> attribute = channel.attr(AttributeKey.valueOf(name));
        if (attribute != null) {
            return attribute.get();
        } else {
            return null;
        }
    }
    public static void set(Channel channel, String name, String value) {
        Attribute<String> attribute = channel.attr(AttributeKey.valueOf(name));
        attribute.set(value);
    }
}
