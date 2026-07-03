package com.accenture.ai.onboardingagentgitclient.config;

import com.accenture.ai.onboardingagentgitclient.enums.AgentName;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "onboarding.docs")
public class AgentRegistry {

    private List<String> repoIntelligence;
    private List<String> systemReadiness;
    private List<String> knowApplication;

    public List<String> docsFor(AgentName agent) {
        return switch (agent) {
            case REPO_INTELLIGENCE -> repoIntelligence;
            case SYSTEM_READINESS -> systemReadiness;
            case KNOW_APPLICATION -> knowApplication;
        };
    }
}
