package com.accenture.ai.onboardingagentgitclient.model;

import lombok.Builder;

@Builder
/*
  Citation
  --------
  Metadata pointing to the source material that was used for answering.
  Helps with:
  - transparency ("where did this answer come from?")
  - debugging wrong answers (which chunk misled the model?)
 */

public record Citation(String document, com.accenture.ai.onboardingagentgitclient.enums.SourceType section, String chunkId) {
}
