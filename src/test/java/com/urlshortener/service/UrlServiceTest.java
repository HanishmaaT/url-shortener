package com.urlshortener.service;

import com.urlshortener.exception.ExpiredUrlException;
import com.urlshortener.exception.InvalidUrlException;
import com.urlshortener.exception.ShortUrlNotFoundException;
import com.urlshortener.model.UrlMapping;
import com.urlshortener.repository.UrlMappingRepository;
import com.urlshortener.validation.UrlValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

        urlService = new UrlService(
                repository,
                generator,
                validator
        );
    }

    @Test
    void shouldCreateShortUrlForValidUrl() {
        String originalUrl = "https://example.com";
        String shortCode = "Ab12Cd3";

        when(validator.isValid(originalUrl))
                .thenReturn(true);

        when(generator.generate())
                .thenReturn(shortCode);

        when(repository.existsByShortCode(shortCode))
                .thenReturn(false);

        UrlMapping mapping =
                new UrlMapping(shortCode, originalUrl);

        when(repository.save(any(UrlMapping.class)))
                .thenReturn(mapping);

        UrlMapping result =
                urlService.createShortUrl(originalUrl);

        assertEquals(
                shortCode,
                result.getShortCode()
        );

        assertEquals(
                originalUrl,
                result.getOriginalUrl()
        );

        verify(repository)
                .save(any(UrlMapping.class));
    }

    @Test
    void shouldRejectInvalidUrl() {
        String invalidUrl = "invalid-url";

        when(validator.isValid(invalidUrl))
                .thenReturn(false);

        assertThrows(
                InvalidUrlException.class,
                () -> urlService.createShortUrl(invalidUrl)
        );

        verify(repository, never())
                .save(any());
    }

    @Test
    void shouldResolveExistingShortCode() {
        UrlMapping mapping =
                new UrlMapping(
                        "Ab12Cd3",
                        "https://example.com"
                );

        when(repository.findByShortCode("Ab12Cd3"))
                .thenReturn(Optional.of(mapping));

        UrlMapping result =
                urlService.resolve("Ab12Cd3");

        assertEquals(
                "https://example.com",
                result.getOriginalUrl()
        );
    }

    @Test
    void shouldThrowExceptionForUnknownShortCode() {
        when(repository.findByShortCode("Unknown"))
                .thenReturn(Optional.empty());

        assertThrows(
                ShortUrlNotFoundException.class,
                () -> urlService.resolve("Unknown")
        );
    }

    @Test
    void shouldCreateUrlWithFutureExpiration() {
        String originalUrl = "https://example.com";
        String shortCode = "Ab12Cd3";

        Instant expiresAt =
                Instant.now().plusSeconds(3600);

        when(validator.isValid(originalUrl))
                .thenReturn(true);

        when(generator.generate())
                .thenReturn(shortCode);

        when(repository.existsByShortCode(shortCode))
                .thenReturn(false);

        UrlMapping mapping =
                new UrlMapping(
                        shortCode,
                        originalUrl,
                        expiresAt
                );

        when(repository.save(any(UrlMapping.class)))
                .thenReturn(mapping);

        UrlMapping result =
                urlService.createShortUrl(
                        originalUrl,
                        expiresAt
                );

        assertEquals(
                expiresAt,
                result.getExpiresAt()
        );
    }

    @Test
    void shouldRejectPastExpiration() {
        String originalUrl = "https://example.com";

        Instant expiresAt =
                Instant.now().minusSeconds(60);

        when(validator.isValid(originalUrl))
                .thenReturn(true);

        assertThrows(
                InvalidUrlException.class,
                () -> urlService.createShortUrl(
                        originalUrl,
                        expiresAt
                )
        );

        verify(repository, never())
                .save(any());
    }

    @Test
    void shouldRejectExpiredUrlDuringResolve() {
        String shortCode = "Ab12Cd3";

        UrlMapping mapping =
                new UrlMapping(
                        shortCode,
                        "https://example.com",
                        Instant.now().minusSeconds(60)
                );

        when(repository.findByShortCode(shortCode))
                .thenReturn(Optional.of(mapping));

        assertThrows(
                ExpiredUrlException.class,
                () -> urlService.resolve(shortCode)
        );
    }
}