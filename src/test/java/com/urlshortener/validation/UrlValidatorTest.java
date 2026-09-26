package com.urlshortener.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UrlValidatorTest {

    private final UrlValidator validator = new UrlValidator();

    @Test
    void shouldAcceptValidHttpAndHttpsUrls() {
        assertTrue(validator.isValid("https://example.com"));
        assertTrue(validator.isValid("http://example.com/path?q=test"));
    }

    @Test
    void shouldRejectUrlsWithoutSupportedScheme() {
        assertFalse(validator.isValid("example.com"));
        assertFalse(validator.isValid("ftp://example.com"));
    }

    @Test
    void shouldRejectMalformedOrEmptyUrls() {
        assertFalse(validator.isValid(""));
        assertFalse(validator.isValid("not a url"));
        assertFalse(validator.isValid(null));
    }
}