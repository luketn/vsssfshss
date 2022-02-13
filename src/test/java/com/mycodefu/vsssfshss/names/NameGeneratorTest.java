package com.mycodefu.vsssfshss.names;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class NameGeneratorTest {
    @Test
    void generateName() {
        String name = NameGenerator.generateName();
        System.out.println(name);

        assertNotNull(name);
        assertTrue(name.length() > 2);
    }

    @Test
    void generateUniqueNames() {
        List<String> nameList = IntStream.range(0, 100000).mapToObj(value -> NameGenerator.generateName()).toList();

        for (int i = 0; i < 1000; i++) {
            String name = NameGenerator.generateName();
            assertFalse(nameList.contains(name), String.format("Generated duplicate name %s", name));
        }
    }
}