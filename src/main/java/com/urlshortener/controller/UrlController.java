package com.urlshortener.controller;

import com.urlshortener.dto.CreateUrlRequest;
import com.urlshortener.dto.CreateUrlResponse;
import com.urlshortener.dto.UrlStatsResponse;
import com.urlshortener.exception.RateLimitExceededException;
import com.urlshortener.model.UrlMapping;
import com.urlshortener.service.RateLimitService;
import com.urlshortener.service.UrlService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/urls")
public class UrlController {

    private final UrlService urlService;
    private final RateLimitService rateLimitService;

    public UrlController(
            UrlService urlService,
            RateLimitService rateLimitService) {

        this.urlService = urlService;
        this.rateLimitService = rateLimitService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateUrlResponse create(
            @Valid @RequestBody CreateUrlRequest request,
            HttpServletRequest httpRequest) {

        String clientId = httpRequest.getRemoteAddr();

        if (!rateLimitService.allowRequest(clientId)) {
            throw new RateLimitExceededException();
        }

        UrlMapping mapping = urlService.createShortUrl(
                request.url(),
                request.expiresAt()
        );

        String shortUrl = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/")
                .path(mapping.getShortCode())
                .toUriString();

        return new CreateUrlResponse(
                mapping.getShortCode(),
                shortUrl,
                mapping.getOriginalUrl(),
                mapping.getCreatedAt(),
                mapping.getExpiresAt()
        );
    }

    @GetMapping("/{shortCode}/stats")
    public UrlStatsResponse getStats(
            @PathVariable String shortCode) {

        UrlMapping mapping =
                urlService.getStats(shortCode);

        return new UrlStatsResponse(
                mapping.getShortCode(),
                mapping.getOriginalUrl(),
                mapping.getClickCount(),
                mapping.getCreatedAt(),
                mapping.getExpiresAt()
        );
    }
}