package com.accenture.ai.onboardingagentgitclient.model;

public record DocChunk(
        String chunkId,
        String document,
        com.accenture.ai.onboardingagentgitclient.enums.SourceType section,     // optional if you later infer headings
        String text
) {}
