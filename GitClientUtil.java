package com.accenture.ai.onboardingagentgitclient.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GitClientUtil {

    private final WebClient client;
    private final ObjectMapper mapper;
    @Value("${github.models.model}")
    private String model;

    public GitClientUtil(@Qualifier("githubWebClient") WebClient client,
                         ObjectMapper mapper) {
        this.client = client;
        this.mapper = mapper;
    }

    public Mono<JsonNode> callChat(List<Map<String, Object>> messages) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("messages", messages);
        body.put("temperature", 0.2);
        return client.post()
                .uri("/inference/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.valueOf("application/vnd.github+json"))
                .bodyValue(body)
                .exchangeToMono(resp ->
                        resp.bodyToMono(String.class).flatMap(raw -> {
                            if (resp.statusCode().is2xxSuccessful()) {
                                try {
                                    return Mono.just(mapper.readTree(raw));
                                } catch (Exception e) {
                                    return Mono.error(e);
                                }
                            }
                            return Mono.error(new RuntimeException(
                                    "GitHub Models error " + resp.statusCode() + ": " + raw));
                        })
                )
                .retryWhen(
                        Retry.backoff(3, Duration.ofSeconds(2))
                                .filter(this::isRetryable)
                );
    }

    public Map<String, Object> msg(String role, String content) {
        return Map.of("role", role, "content", content);
    }

    public String extractText(JsonNode response) {
        JsonNode content = response.at("/choices/0/message/content");
        return content.isMissingNode() ? response.toString() : content.asText();
    }

    public List<String> splitSteps(String plan) {
        return Arrays.stream(plan.split("\n"))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(s -> s.replaceFirst("^[\\-*\\d.)\\s]+", ""))
                .toList();
    }


    private boolean isRetryable(Throwable t) {
        // Network-level issues (DNS, connection reset, timeouts, etc.)
        if (t instanceof WebClientRequestException) {
            return true;
        }

        // HTTP 5xx from server (transient upstream failures)
        if (t instanceof WebClientResponseException ex) {
            return ex.getStatusCode().is5xxServerError();
        }

        return false;
    }
}
