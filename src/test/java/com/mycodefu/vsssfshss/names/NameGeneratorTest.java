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
        assertTrue(name.matches("^[A-Z][a-z]+[A-Z][a-z]+\\d{3}$"));
    }
}