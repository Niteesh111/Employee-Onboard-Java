package com.accenture.ai.onboardingagentgitclient.service;

import com.accenture.ai.onboardingagentgitclient.enums.SourceType;
import com.accenture.ai.onboardingagentgitclient.model.SourceRequest;

import java.util.List;

public interface SourceExtractor {
    SourceType supports();
    List<String> extract(SourceRequest request);

}
