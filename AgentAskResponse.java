package com.accenture.ai.onboardingagentgitclient.model;

import java.util.List;

public record AgentAskResponse(
        String model,
        String reply,
        List<String> nextSteps
) {}
