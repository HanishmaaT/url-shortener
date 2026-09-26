package com.urlshortener.validation;

import org.springframework.stereotype.Component;

import java.net.URI;

@Component
public class UrlValidator {

    public boolean isValid(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }

        try {
            URI uri = URI.create(url);

            String scheme = uri.getScheme();

            if (scheme == null ||
                    (!scheme.equalsIgnoreCase("http")
                            && !scheme.equalsIgnoreCase("https"))) {
                return false;
            }

            return uri.getHost() != null && !uri.getHost().isBlank();

        } catch (IllegalArgumentException exception) {
            return false;
        }
    }
}