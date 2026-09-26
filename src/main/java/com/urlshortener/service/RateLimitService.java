package com.urlshortener.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class RateLimitService {

    private static final int MAX_REQUESTS = 20;
    private static final long WINDOW_SECONDS = 60;

    private final ConcurrentHashMap<String, RequestWindow> clients =
            new ConcurrentHashMap<>();

    public boolean allowRequest(String clientId) {

        long currentWindow =
                Instant.now().getEpochSecond() / WINDOW_SECONDS;

        RequestWindow window = clients.compute(
                clientId,
                (key, existing) -> {

                    if (existing == null ||
                            existing.windowId != currentWindow) {

                        return new RequestWindow(
                                currentWindow,
                                new AtomicInteger(1)
                        );
                    }

                    existing.requestCount.incrementAndGet();
                    return existing;
                }
        );

        return window.requestCount.get() <= MAX_REQUESTS;
    }

    private static class RequestWindow {

        private final long windowId;
        private final AtomicInteger requestCount;

        private RequestWindow(
                long windowId,
                AtomicInteger requestCount) {

            this.windowId = windowId;
            this.requestCount = requestCount;
        }
    }
}