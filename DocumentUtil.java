package com.accenture.ai.onboardingagentgitclient.util;

import com.accenture.ai.onboardingagentgitclient.config.AgentRegistry;
import com.accenture.ai.onboardingagentgitclient.enums.AgentName;
import com.accenture.ai.onboardingagentgitclient.enums.SourceType;
import com.accenture.ai.onboardingagentgitclient.model.DocChunk;
import com.accenture.ai.onboardingagentgitclient.model.SourceRequest;
import com.accenture.ai.onboardingagentgitclient.service.SourceExtractor;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class DocumentUtil {

    private final AgentRegistry registry;

    // Interface-based routing
    private final Map<SourceType, SourceExtractor> extractorMap =
            new ConcurrentHashMap<>();

    // Cached chunks
    private final Map<String, List<DocChunk>> docChunksByLocation =
            new ConcurrentHashMap<>();

    public DocumentUtil(
            AgentRegistry registry,
            List<SourceExtractor> extractors
    ) {
        this.registry = registry;
        for (SourceExtractor extractor : extractors) {
            log.info("Registering source extractor {}", extractor);
            extractorMap.put(extractor.supports(), extractor);
        }
    }


    @PostConstruct
    public void preload() {
        for (AgentName agent : AgentName.values()) {
            for (String loc : registry.docsFor(agent)) {
                docChunksByLocation.computeIfAbsent(loc, this::loadAndChunk);
            }
        }
    }

    public List<DocChunk> retrieve(AgentName agent, String userPrompt, int topK) {
        List<String> allowed = registry.docsFor(agent);

        List<DocChunk> all = new ArrayList<>();
        for (String loc : allowed) {
            all.addAll(docChunksByLocation.computeIfAbsent(loc, this::loadAndChunk));
        }

        return Retriever.topK(all, userPrompt, topK);
    }


    public List<DocChunk> loadAndChunk(String loc) {
        log.info("Loading and chunking document: {}", loc);
        SourceType type = resolveSourceType(loc);
        log.info("Resolved source type: {} for location: {}", type, loc);
        SourceExtractor extractor = extractorMap.get(type);

        if (extractor == null) {
            throw new IllegalArgumentException(
                    "No SourceExtractor registered for " + type);
        }

        List<String> contents =
                extractor.extract(new SourceRequest(type, loc));

        List<DocChunk> chunks = new ArrayList<>();
        int i = 0;

        for (String text : contents) {
            chunks.add(new DocChunk(
                    loc + "#c" + (i++),
                    loc,
                    type,
                    text
            ));
        }

        return chunks;
    }


    private SourceType resolveSourceType(String loc) {
        String lower = loc.toLowerCase();

        if (lower.endsWith(".docx")) return SourceType.WORD;
        if (lower.endsWith(".xlsx") || lower.endsWith(".xls")) return SourceType.EXCEL;
        return null;
    }

}