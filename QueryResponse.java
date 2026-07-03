package com.accenture.ai.onboardingagentgitclient.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;

@Value
@Builder
@AllArgsConstructor

/*
  Outgoing payload.
  - reply: what the FE displays
  - agent: which agent handled the request (useful for logging/debugging)
  - followUpPrompts: MUST be 3–5 items (enforced in orchestrator)
 */
 public class QueryResponse {
     String reply;
     String agent;
     @Builder.Default
     List<String> followUpPrompts = new ArrayList<>();
     @Builder.Default
     List<Citation> citations = new ArrayList<>();
}
