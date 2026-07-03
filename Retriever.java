package com.accenture.ai.onboardingagentgitclient.util;

import com.accenture.ai.onboardingagentgitclient.model.DocChunk;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component

/*
 * =========================
 * Retriever – Step by Step
 * =========================
 *
 * Purpose:
 * --------
 * The Retriever selects ONLY the most relevant text chunks (paragraphs)
 * from Word documents before sending them to the LLM.
 *
 * This prevents:
 * - Sending entire documents
 * - Wasting tokens
 *
 * Step-by-step flow:
 * ------------------
 * 1. Take the user's prompt (question).
 *    Example:
 *    "What IDE plugins do I need?"
 *
 * 2. Break the user prompt into meaningful keywords.
 *    Example:
 *    ["ide", "plugins", "need", "coding"]
 *
 * 3. For each document chunk (paragraph):
 *    - Break the paragraph into keywords
 *    - Compare paragraph keywords with prompt keywords
 *
 * 4. Calculate a simple relevance score:
 *    - Count how many keywords overlap
 *    - More overlap = higher relevance
 *
 * 5. Ignore chunks with zero relevance.
 *
 * 6. Sort remaining chunks by relevance score (highest first).
 *
 * 7. Return only the top K chunks (e.g., top 5 or 6).
 *
 * 8. These selected chunks are passed to the LLM as CONTEXT.
 *
 * What Retriever DOES:
 * -------------------
 * - Filters documents
 * - Selects relevant paragraphs
 * - Reduces noise
 * - Improves answer quality
 *
 * What Retriever DOES NOT do:
 * --------------------------
 * - Does not generate answers
 * - Does not call the LLM
 * - Does not understand meaning (keyword-based)
 * - Does not modify content

 * In short:
 * ---------
 * Retriever decides:
 * "Which text is worth showing to the LLM?"
 */

public class Retriever {

    public static List<DocChunk> topK(List<DocChunk> chunks, String userPrompt, int k) {
        Set<String> userTokens = tokenize(userPrompt);

        var docChunks = chunks.stream()
                .map(c -> Map.entry(c, score(c.text(), userTokens)))
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .filter(e -> e.getValue() > 0)
                .limit(k)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        log.info("Top {} relevant chunks for prompt '{}': {}",
                k, userPrompt, !CollectionUtils.isEmpty(docChunks)?docChunks.stream().map(DocChunk::chunkId).toList():null);
        return docChunks;
    }

    private static double score(String docText, Set<String> userTokens) {
        Set<String> docTextSet = tokenize(docText);
        int overlap = 0;
        for (String t : userTokens) if (docTextSet.contains(t)) overlap++;
        return overlap;
    }

    //Get keywords from the string and store in set to avoid duplicates
    private static Set<String> tokenize(String s) {
        if (s == null) return Set.of();

        return Arrays.stream(s.toLowerCase(Locale.ROOT).split("[^a-z0-9]+"))
                .filter(x -> x.length() >= 3) // remove tiny words like "a", "to", "is"
                .collect(Collectors.toSet());
    }
}
