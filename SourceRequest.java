package com.accenture.ai.onboardingagentgitclient.model;

import com.accenture.ai.onboardingagentgitclient.enums.SourceType;
import lombok.Value;

/**
 * SourceRequest
 * ----------------
 * Represents a single source input to the document ingestion pipeline.
 *
 * - `sourceType` : WORD / EXCEL / PDF (may be resolved from location if needed)
 * - `location`   : file path / classpath / URL
 */
@Value
public class SourceRequest{
        SourceType sourceType;
        String location;
}