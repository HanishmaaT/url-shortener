package com.urlshortener.integration;

import com.urlshortener.model.UrlMapping;
import com.urlshortener.repository.UrlMappingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UrlShortenerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UrlMappingRepository repository;

    @BeforeEach
    void cleanDatabase() {
        repository.deleteAll();
    }

    @Test
    void shouldCreateAndRedirectShortUrl() throws Exception {

        String response = mockMvc.perform(
                        post("/api/urls")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "url": "https://example.com"
                                        }
                                        """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shortCode").exists())
                .andExpect(
                        jsonPath("$.originalUrl")
                                .value("https://example.com")
                )
                .andReturn()
                .getResponse()
                .getContentAsString();

        String shortCode = response
                .split("\"shortCode\":\"")[1]
                .split("\"")[0];

        mockMvc.perform(get("/" + shortCode))
                .andExpect(status().isFound())
                .andExpect(
                        header().string(
                                "Location",
                                "https://example.com"
                        )
                );
    }

    @Test
    void shouldRejectInvalidUrl() throws Exception {

        mockMvc.perform(
                        post("/api/urls")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "url": "hello"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.title")
                                .value("Invalid URL")
                );
    }

    @Test
    void shouldReturn404ForUnknownShortCode() throws Exception {

        mockMvc.perform(get("/ZZZZZZZ"))
                .andExpect(status().isNotFound())
                .andExpect(
                        jsonPath("$.title")
                                .value("Short URL Not Found")
                );
    }

    @Test
    void shouldReturn410ForExpiredUrl() throws Exception {

        String response = mockMvc.perform(
                        post("/api/urls")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "url": "https://example.com",
                                          "expiresAt": "2099-01-01T00:00:00Z"
                                        }
                                        """)
                )
                .andExpect(status().isCreated())
                .andExpect(
                        jsonPath("$.expiresAt")
                                .value("2099-01-01T00:00:00Z")
                )
                .andReturn()
                .getResponse()
                .getContentAsString();

        String shortCode = response
                .split("\"shortCode\":\"")[1]
                .split("\"")[0];

        UrlMapping existingMapping = repository
                .findByShortCode(shortCode)
                .orElseThrow();

        repository.delete(existingMapping);

        UrlMapping expiredMapping = new UrlMapping(
                shortCode,
                existingMapping.getOriginalUrl(),
                Instant.now().minusSeconds(60)
        );

        repository.save(expiredMapping);

        mockMvc.perform(get("/" + shortCode))
                .andExpect(status().isGone())
                .andExpect(
                        jsonPath("$.title")
                                .value("Short URL Expired")
                );
    }
}