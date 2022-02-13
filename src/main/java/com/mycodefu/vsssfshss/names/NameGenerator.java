package com.mycodefu.vsssfshss.names;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Random;

public class NameGenerator {
    private static String[] adjectives = stringsFromTextResource("/names/adjectives.txt");
    private static String[] nouns = stringsFromTextResource("/names/nouns.txt");
    private static long seed = System.currentTimeMillis();

    private static String[] stringsFromTextResource(String resourcePath) {
        try (InputStream resource = NameGenerator.class.getResourceAsStream(resourcePath);
             InputStreamReader inputStreamReader = new InputStreamReader(resource);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {

            List<String> lines = bufferedReader.lines().toList();
            return lines.toArray(new String[]{});

        } catch (IOException e) {
            throw new RuntimeException("Failed to load " + resourcePath, e);
        }
    }

    public static String generateName() {
        Random random = new Random(seed++);
        return adjectives[random.nextInt(adjectives.length)] +
               nouns[random.nextInt(nouns.length)] +
               String.format("%04d",random.nextInt(10000));
    }
}
