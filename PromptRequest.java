package com.accenture.ai.onboardingagentgitclient.model;


import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * PromptRequest
 * ------------
 * Incoming request payload from FE (or other callers).
 * <p>
 * Only required field is "prompt".
 * Optional fields can be used later for personalization.
 */
public record PromptRequest(@JsonProperty("name") String name, @JsonProperty("question") String prompt) {
}
