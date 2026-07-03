package com.accenture.ai.onboardingagentgitclient.model;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public record AccessRequest(String requestId, String name, String email) {
}
