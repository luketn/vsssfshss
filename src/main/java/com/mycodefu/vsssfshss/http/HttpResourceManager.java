package com.mycodefu.vsssfshss.http;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HttpResourceManager {
    private static final Map<String, byte[]> resourceCache = new ConcurrentHashMap<>();

    public static byte[] getResource(String path) {
        if (resourceCache.containsKey(path)) {
            return resourceCache.get(path);
        } else {
            byte[] resourceContents = readResource(path);
            if (resourceContents != null) {
                resourceCache.put(path, resourceContents);
            }
            return resourceContents;
        }
    }

    private static byte[] readResource(String path) {
        byte[] content;
        try (InputStream input = HttpResourceManager.class.getResourceAsStream(path);
                BufferedInputStream bufferedInput = new BufferedInputStream(input)) {
            content = bufferedInput.readAllBytes();
        } catch (IOException e) {
            content = null;
        }
        return content;
    }
}
