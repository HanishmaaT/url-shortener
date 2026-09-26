package com.urlshortener.service;

import com.urlshortener.exception.InvalidUrlException;
import com.urlshortener.exception.ShortCodeGenerationException;
import com.urlshortener.exception.ShortUrlNotFoundException;
import com.urlshortener.model.UrlMapping;
import com.urlshortener.repository.UrlMappingRepository;
import com.urlshortener.validation.UrlValidator;
import org.springframework.stereotype.Service;

@Service
public class UrlService {

    private static final int MAX_GENERATION_ATTEMPTS = 10;

    private final UrlMappingRepository repository;
    private final ShortCodeGenerator shortCodeGenerator;
    private final UrlValidator urlValidator;

    public UrlService(
            UrlMappingRepository repository,
            ShortCodeGenerator shortCodeGenerator,
            UrlValidator urlValidator) {

        this.repository = repository;
        this.shortCodeGenerator = shortCodeGenerator;
        this.urlValidator = urlValidator;
    }

    public UrlMapping createShortUrl(String originalUrl) {

        if (!urlValidator.isValid(originalUrl)) {
            throw new InvalidUrlException(
                    "URL must be a valid HTTP or HTTPS URL");
        }

        for (int attempt = 0; attempt < MAX_GENERATION_ATTEMPTS; attempt++) {

            String shortCode = shortCodeGenerator.generate();

            if (!repository.existsByShortCode(shortCode)) {
                UrlMapping mapping =
                        new UrlMapping(shortCode, originalUrl);

                return repository.save(mapping);
            }
        }

        throw new ShortCodeGenerationException();
    }

    public UrlMapping resolve(String shortCode) {
        return repository.findByShortCode(shortCode)
                .orElseThrow(() ->
                        new ShortUrlNotFoundException(shortCode));
    }
}