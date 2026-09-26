package com.urlshortener.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RateLimitServiceTest {

    @Test
    void shouldAllowRequestsWithinLimitAndRejectExcessRequest() {

        RateLimitService rateLimitService =
                new RateLimitService();

        String clientId = "test-client";

        for (int i = 0; i < 20; i++) {
            assertTrue(
                    rateLimitService.allowRequest(clientId)
            );
        }

        assertFalse(
                rateLimitService.allowRequest(clientId)
        );
    }

    @Test
    void shouldTrackClientsIndependently() {

        RateLimitService rateLimitService =
                new RateLimitService();

        for (int i = 0; i < 20; i++) {
            assertTrue(
                    rateLimitService.allowRequest("client-A")
            );
        }

        assertFalse(
                rateLimitService.allowRequest("client-A")
        );

        assertTrue(
                rateLimitService.allowRequest("client-B")
        );
    }
}