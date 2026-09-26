package com.urlshortener.service;

import com.urlshortener.repository.UrlMappingRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnalyticsService {

    private final UrlMappingRepository repository;

    public AnalyticsService(UrlMappingRepository repository) {
        this.repository = repository;
    }

    @Async("analyticsExecutor")
    @Transactional
    public void recordRedirect(Long mappingId) {
        repository.incrementClickCount(mappingId);
    }
}