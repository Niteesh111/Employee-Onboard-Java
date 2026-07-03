package com.accenture.ai.onboardingagentgitclient.util;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;

@Component
public class ConfluenceClientUtil {

    private final WebClient webClient;

    public ConfluenceClientUtil(@Qualifier("confluenceWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    @SuppressWarnings("unchecked")
    public String fetchStorageContent(String pageId) {

        Map<String, Object> response = webClient.get()
                .uri("/rest/api/content/{id}?expand=body.storage", pageId)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
        if(Objects.isNull(response)) {
            return null;
        }
        Map<String, Object> body =
                (Map<String, Object>) response.get("body");
        Map<String, Object> storage =
                (Map<String, Object>) body.get("storage");

        return storage.get("value").toString();
    }
}