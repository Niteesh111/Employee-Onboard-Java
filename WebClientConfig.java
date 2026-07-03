package com.accenture.ai.onboardingagentgitclient.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Configuration
public class WebClientConfig {


    @Bean
    @Qualifier("githubWebClient")
    WebClient githubModelsWebClient(
            @Value("${github.models.base-url}") String baseUrl,
            @Value("${github.models.token}") String token,
            @Value("${github.models.api-version}") String apiVersion
    ) {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github+json")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .defaultHeader("X-GitHub-Api-Version", apiVersion)
                .build();
    }

    @Bean
    @Qualifier("confluenceWebClient")
    WebClient confluenceWebClient(
            @Value("${onboarding.confluence.base-url}") String baseUrl,
            @Value("${onboarding.confluence.email}") String email,
            @Value("${onboarding.confluence.api-token}") String token
    ) {
        String auth = Base64.getEncoder()
                .encodeToString((email + ":" + token)
                        .getBytes(StandardCharsets.UTF_8));
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + auth)
                .build();
    }
}
