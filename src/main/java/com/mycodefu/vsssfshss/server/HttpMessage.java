package com.mycodefu.vsssfshss.server;

import java.util.Map;
import io.netty.handler.codec.http.HttpResponseStatus;

public record HttpMessage(HttpResponseStatus status, byte[] content, Map<String, Object> headers) {
    public HttpMessage {
        // Make the headers map imutable
        headers = Map.copyOf(headers);
    }
}
