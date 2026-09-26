package com.urlshortener.integration;

import com.urlshortener.repository.UrlMappingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

        String response = mockMvc.perform(post("/api/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "url": "https://example.com"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shortCode").exists())
                .andExpect(jsonPath("$.originalUrl")
                        .value("https://example.com"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String shortCode = response
                .split("\"shortCode\":\"")[1]
                .split("\"")[0];

        mockMvc.perform(get("/" + shortCode))
                .andExpect(status().isFound())
                .andExpect(header()
                        .string("Location", "https://example.com"));
    }

    @Test
    void shouldRejectInvalidUrl() throws Exception {

        mockMvc.perform(post("/api/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "url": "hello"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title")
                        .value("Invalid URL"));
    }

    @Test
    void shouldReturn404ForUnknownShortCode() throws Exception {

        mockMvc.perform(get("/ZZZZZZZ"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title")
                        .value("Short URL Not Found"));
    }
}