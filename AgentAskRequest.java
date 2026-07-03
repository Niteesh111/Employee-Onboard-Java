package com.accenture.ai.onboardingagentgitclient.model;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record AgentAskRequest(
        @NotBlank String prompt,
        String systemInstruction,
        List<ToolSpec> tools
) {
    public record ToolSpec(
            @NotBlank String name,
            @NotBlank String description
    ) {}
}
