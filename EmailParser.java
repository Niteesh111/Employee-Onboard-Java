package com.accenture.ai.onboardingagentgitclient.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class EmailParser {

    public Map<String, Object> extractEmailDetails(String rawString) {
        Map<String, Object> emailMap = new HashMap<>();

        emailMap.put("subject", parseField(rawString, "Subject:"));
        emailMap.put("to", parseField(rawString, "To:"));
        emailMap.put("cc", parseField(rawString, "CC:"));
        emailMap.put("bcc", parseField(rawString, "Bcc:"));
        emailMap.put("content", parseField(rawString, "Body:"));
        log.info("Required details to send Email: {}", emailMap);

        return emailMap;
    }

    private String parseField(String source, String label) {
        Pattern pattern = Pattern.compile(label + "\\s*(.*)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(source);
        if (matcher.find()) {
            String value = matcher.group(1).trim();
            return value.contains("Not Found") ? null : value;
        }
        return null;
    }


    private void validateField(Map<String, Object> map, String key) throws Exception {
        if (map.get(key) == null || map.get(key).toString().isEmpty()) {
            throw new Exception("Critical Error: Required field '" + key + "' was not found in the document.");
        }
    }
}
