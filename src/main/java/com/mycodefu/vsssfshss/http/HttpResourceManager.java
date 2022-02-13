package com.mycodefu.vsssfshss.http;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class HttpResourceManager {
    private static final Map<String, byte[]> resourceCache = new HashMap<>();

    public static byte[] getResource(String path) {
        if (resourceCache.containsKey(path)) {
            return resourceCache.get(path);
        } else {
            byte[] resourceContents = readResource(path);
            resourceCache.put(path, resourceContents);
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
