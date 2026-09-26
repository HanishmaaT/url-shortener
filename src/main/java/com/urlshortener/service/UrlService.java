package com.urlshortener.service;

import com.urlshortener.exception.ExpiredUrlException;
import com.urlshortener.exception.InvalidUrlException;
import com.urlshortener.exception.ShortCodeGenerationException;
import com.urlshortener.exception.ShortUrlNotFoundException;
import com.urlshortener.model.UrlMapping;
import com.urlshortener.repository.UrlMappingRepository;
import com.urlshortener.validation.UrlValidator;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class UrlService {

    private static final int MAX_GENERATION_ATTEMPTS = 10;

    private final UrlMappingRepository repository;
    private final ShortCodeGenerator shortCodeGenerator;
    private final UrlValidator urlValidator;
    private final AnalyticsService analyticsService;

    public UrlService(
            UrlMappingRepository repository,
            ShortCodeGenerator shortCodeGenerator,
            UrlValidator urlValidator,
            AnalyticsService analyticsService) {

        this.repository = repository;
        this.shortCodeGenerator = shortCodeGenerator;
        this.urlValidator = urlValidator;
        this.analyticsService = analyticsService;
    }

    public UrlMapping createShortUrl(String originalUrl) {
        return createShortUrl(originalUrl, null);
    }

    public UrlMapping createShortUrl(
            String originalUrl,
            Instant expiresAt) {

        if (!urlValidator.isValid(originalUrl)) {
            throw new InvalidUrlException(
                    "URL must be a valid HTTP or HTTPS URL"
            );
        }

        if (expiresAt != null &&
                !expiresAt.isAfter(Instant.now())) {

            throw new InvalidUrlException(
                    "Expiration time must be in the future"
            );
        }

        for (int attempt = 0;
             attempt < MAX_GENERATION_ATTEMPTS;
             attempt++) {

            String shortCode = shortCodeGenerator.generate();

            if (!repository.existsByShortCode(shortCode)) {

                UrlMapping mapping = new UrlMapping(
                        shortCode,
                        originalUrl,
                        expiresAt
                );

                return repository.save(mapping);
            }
        }

        throw new ShortCodeGenerationException();
    }

    public UrlMapping resolve(String shortCode) {

        UrlMapping mapping = findByShortCode(shortCode);

        if (mapping.isExpired(Instant.now())) {
            throw new ExpiredUrlException(shortCode);
        }

        analyticsService.recordRedirect(mapping.getId());

        return mapping;
    }

    public UrlMapping getStats(String shortCode) {
        return findByShortCode(shortCode);
    }

    private UrlMapping findByShortCode(String shortCode) {
        return repository.findByShortCode(shortCode)
                .orElseThrow(
                        () -> new ShortUrlNotFoundException(shortCode)
                );
    }
}