package com.accenture.ai.onboardingagentgitclient.util;


import com.accenture.ai.onboardingagentgitclient.enums.AgentName;
import com.accenture.ai.onboardingagentgitclient.model.DocChunk;

import java.util.List;
import java.util.stream.Collectors;


public class SystemPrompts {
    public static String forAgent(AgentName agent) {
        return switch (agent) {
            case REPO_INTELLIGENCE -> """
                You are the Repo Intelligence agent.
                Answer using ONLY the provided document context.
                If information is missing, say so.
                Output STRICT JSON with fields: reply (string), followUpPrompts (array of strings).
                """;
            case SYSTEM_READINESS -> """
                You are the System Readiness agent.Answer using ONLY the provided document context.
                Provide a checklist: required applications, IDE plugins, prerequisites, and setup steps.
                Output STRICT JSON with fields: reply (string), followUpPrompts (array of strings).
                """;
            case KNOW_APPLICATION -> """
                You are the Know the Application agent.
                Answer using ONLY the provided document context.
                Cover architecture (BFF focus when present), key functionalities, libraries/interfaces, microservices necessity, and application flow.
                Output STRICT JSON with fields: reply (string), followUpPrompts (array of strings).
                """;
        };
    }

    public static List<String> buildTemplatePrompt(AgentName agent) {
        return switch (agent) {
            case REPO_INTELLIGENCE -> List.of(
                    "Which repositories are mandatory to clone first?",
                    "What is the core purpose of each required repository?"
            );
            case SYSTEM_READINESS -> List.of(
                    "What are the required applications and versions for local setup?",
                    "Which IDE plugins are recommended for this codebase?"
            );
            case KNOW_APPLICATION -> List.of(
                    "Explain the end-to-end application flow at a high level."
            );
        };
    }

    public static String buildUserMessage(String userPrompt, List<DocChunk> chunks) {
        String context = chunks.stream()
                .map(c -> "SOURCE: " + c.document() + " CHUNK: " + c.chunkId() + "\n" + c.text())
                .collect(Collectors.joining("\n\n---\n\n"));

        return """
               USER_PROMPT:
               %s

               CONTEXT (use only this):
               %s
               """.formatted(userPrompt, context);
    }
}
