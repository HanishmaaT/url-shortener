package com.urlshortener.service;

import com.urlshortener.exception.InvalidUrlException;
import com.urlshortener.exception.ShortUrlNotFoundException;
import com.urlshortener.model.UrlMapping;
import com.urlshortener.repository.UrlMappingRepository;
import com.urlshortener.validation.UrlValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UrlServiceTest {

    private UrlMappingRepository repository;
    private ShortCodeGenerator generator;
    private UrlValidator validator;
    private UrlService urlService;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(UrlMappingRepository.class);
        generator = Mockito.mock(ShortCodeGenerator.class);
        validator = Mockito.mock(UrlValidator.class);

        urlService = new UrlService(repository, generator, validator);
    }

    @Test
    void shouldCreateShortUrlForValidUrl() {
        String originalUrl = "https://example.com";
        String shortCode = "Ab12Cd3";

        when(validator.isValid(originalUrl)).thenReturn(true);
        when(generator.generate()).thenReturn(shortCode);
        when(repository.existsByShortCode(shortCode)).thenReturn(false);

        UrlMapping mapping = new UrlMapping(shortCode, originalUrl);
        when(repository.save(any(UrlMapping.class))).thenReturn(mapping);

        UrlMapping result = urlService.createShortUrl(originalUrl);

        assertEquals(shortCode, result.getShortCode());
        assertEquals(originalUrl, result.getOriginalUrl());

        verify(repository).save(any(UrlMapping.class));
    }

    @Test
    void shouldRejectInvalidUrl() {
        String invalidUrl = "invalid-url";

        when(validator.isValid(invalidUrl)).thenReturn(false);

        assertThrows(
                InvalidUrlException.class,
                () -> urlService.createShortUrl(invalidUrl));

        verify(repository, never()).save(any());
    }

    @Test
    void shouldResolveExistingShortCode() {
        UrlMapping mapping =
                new UrlMapping("Ab12Cd3", "https://example.com");

        when(repository.findByShortCode("Ab12Cd3"))
                .thenReturn(Optional.of(mapping));

        UrlMapping result = urlService.resolve("Ab12Cd3");

        assertEquals("https://example.com", result.getOriginalUrl());
    }

    @Test
    void shouldThrowExceptionForUnknownShortCode() {
        when(repository.findByShortCode("Unknown"))
                .thenReturn(Optional.empty());

        assertThrows(
                ShortUrlNotFoundException.class,
                () -> urlService.resolve("Unknown"));
    }
}