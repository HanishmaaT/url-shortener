package com.urlshortener.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

public record CreateUrlRequest(
        @NotBlank(message = "URL is required")
        String url,

        Instant expiresAt
) {
}