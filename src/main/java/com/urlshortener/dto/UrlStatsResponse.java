package com.urlshortener.dto;

import java.time.Instant;

public record UrlStatsResponse(
        String shortCode,
        String originalUrl,
        long redirectCount,
        Instant createdAt,
        Instant expiresAt
) {
}