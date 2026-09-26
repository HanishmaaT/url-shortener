package com.urlshortener.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShortCodeGeneratorTest {

    private final ShortCodeGenerator generator = new ShortCodeGenerator();

    @Test
    void shouldGenerateSevenCharacterCode() {
        String code = generator.generate();

        assertEquals(7, code.length());
    }

    @Test
    void shouldGenerateOnlyBase62Characters() {
        String code = generator.generate();

        assertTrue(code.matches("[0-9A-Za-z]{7}"));
    }

    @Test
    void shouldGenerateDifferentCodes() {
        String first = generator.generate();
        String second = generator.generate();

        assertNotEquals(first, second);
    }
}