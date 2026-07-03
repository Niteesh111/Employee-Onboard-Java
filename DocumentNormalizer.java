package com.accenture.ai.onboardingagentgitclient.util;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class DocumentNormalizer {

    public String toLLMContext(List<Map<String, String>> rows) {
        StringBuilder sb = new StringBuilder();

        for (Map<String, String> row : rows) {
            row.forEach((k, v) ->
                    sb.append(k).append(": ").append(v).append(" | ")
            );
            sb.append("\n");
        }
        return sb.toString();
    }
}
