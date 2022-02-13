package com.mycodefu.vsssfshss.names;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Random;

public class NameGenerator {
    private static String[] adjectives = stringsFromTextResource(NameGenerator.class.getResourceAsStream("/names/adjectives.txt"));
    private static String[] nouns = stringsFromTextResource(NameGenerator.class.getResourceAsStream("/names/nouns.txt"));
    private static Random random = new Random();

    private static String[] stringsFromTextResource(InputStream resource) {
        InputStreamReader inputStreamReader = new InputStreamReader(resource);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        List<String> lines = bufferedReader.lines().toList();
        return lines.toArray(new String[]{});
    }

    public static String generateName() {
        return adjectives[random.nextInt(adjectives.length)] + nouns[random.nextInt(nouns.length)];
    }
}
