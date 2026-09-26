package com.urlshortener.controller;

import com.urlshortener.dto.CreateUrlRequest;
import com.urlshortener.dto.CreateUrlResponse;
import com.urlshortener.model.UrlMapping;
import com.urlshortener.service.UrlService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/urls")
public class UrlController {

    private final UrlService urlService;

    public UrlController(UrlService urlService) {
        this.urlService = urlService;
    }

    @PostMapping
    public ResponseEntity<CreateUrlResponse> create(
            @Valid @RequestBody CreateUrlRequest request) {

        UrlMapping mapping = urlService.createShortUrl(
        request.url(),
        request.expiresAt()
        );

        String shortUrl = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/{shortCode}")
                .buildAndExpand(mapping.getShortCode())
                .toUriString();

        CreateUrlResponse response = new CreateUrlResponse(
                mapping.getShortCode(),
                shortUrl,
                mapping.getOriginalUrl(),
                mapping.getCreatedAt(),
                mapping.getExpiresAt()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}