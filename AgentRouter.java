package com.accenture.ai.onboardingagentgitclient.util;

import com.accenture.ai.onboardingagentgitclient.enums.AgentName;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class AgentRouter {

    public AgentName route(String prompt) {
        String p = prompt == null ? "" : prompt.toLowerCase(Locale.ROOT);

        // Repo Intelligence
        if (containsAny(p, "repo", "repository", "repositories", "clone", "codebase", "github")) {
            return AgentName.REPO_INTELLIGENCE;
        }

        // System readiness
        if (containsAny(p, "setup", "install", "ide", "plugin", "docker", "build", "maven", "gradle", "node", "java", "k8s", "kubernetes")) {
            return AgentName.SYSTEM_READINESS;
        }

        // Default: Know the Application (flows, features, architecture, glossary like AFS)
        return AgentName.KNOW_APPLICATION;
    }

    private boolean containsAny(String text, String... keys) {
        for (String k : keys) {
            if (text.contains(k)) return true;
        }
        return false;
    }
}
