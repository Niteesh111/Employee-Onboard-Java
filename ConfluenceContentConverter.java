package com.accenture.ai.onboardingagentgitclient.util;

import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ConfluenceContentConverter {

    public String toPlainText(String xhtml) {
        return Jsoup.parse(xhtml)
                .text()
                .replaceAll("\\s+", " ")
                .trim();
    }

    public List<String> toChunks(String text, int chunkSize) {
        List<String> chunks = new ArrayList<>();
        for (int i = 0; i < text.length(); i += chunkSize) {
            chunks.add(text.substring(i, Math.min(text.length(), i + chunkSize)));
        }
        return chunks;
    }
}