package com.accenture.ai.onboardingagentgitclient.service;

import com.accenture.ai.onboardingagentgitclient.enums.AgentName;
import com.accenture.ai.onboardingagentgitclient.model.Citation;
import com.accenture.ai.onboardingagentgitclient.model.DocChunk;
import com.accenture.ai.onboardingagentgitclient.model.PromptRequest;
import com.accenture.ai.onboardingagentgitclient.model.QueryResponse;
import com.accenture.ai.onboardingagentgitclient.util.AgentRouter;
import com.accenture.ai.onboardingagentgitclient.util.DocumentUtil;
import com.accenture.ai.onboardingagentgitclient.util.GitClientUtil;
import com.accenture.ai.onboardingagentgitclient.util.SystemPrompts;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * AgentOrchestrator
 * -----------------
 * Coordinates the end-to-end processing:
 * 1) Decide agent in backend (AgentRouter)
 * 2) Retrieve relevant doc chunks (DocStore + topK)
 * 3) Build prompts (SystemPrompts + PromptBuilder)
 * 4) Call LLM (LLMClient)
 * 5) Parse output + enforce 3–5 follow-ups
 * 6) Return reply + citations
 */
@Service
@AllArgsConstructor
@Slf4j
public class AgentOrchestratorService {

    private final AgentRouter router;
    private final DocumentUtil documentUtil;
    private final GitClientUtil gitClientUtil;

    record Parsed(String reply, List<String> followUpPrompts) {}

    public QueryResponse handle(PromptRequest req) {
        String prompt = req.prompt();

        // Backend decides agent (as you asked)
        AgentName agent = router.route(prompt);

        /*
          We do NOT send entire documents to the LLM.
          Even within a single Word document, only a few paragraphs
          are relevant to a specific user question.
          docStore.retrieve ensures:
          - Only relevant sections are sent
          - Lower token usage
          - More focused and predictable answers
         */
        List<DocChunk> chunks = documentUtil.retrieve(agent, prompt, 6);

        String system = SystemPrompts.forAgent(agent);
        String userMsg = SystemPrompts.buildUserMessage(prompt, chunks);

        String raw = gitClientUtil.callChat(List.of(
                        Map.of("role", "system", "content", system),
                        Map.of("role", "user", "content", userMsg)
                ))
                .map(gitClientUtil::extractText).block();
        log.info("Response from LLM: {}", raw);
        // Parse JSON from model output (strict)
        Parsed parsed = parseModelJson(raw);

        // Enforce 3–5 follow-ups
        List<String> followUps = ensure3to5(parsed.followUpPrompts(), agent);


        // Build citations so we can track which documents and paragraphs
        // were used to generate the LLM response (for transparency & debugging)
        var citations = chunks.stream()
                .map(c -> new Citation(c.document(), c.section(), c.chunkId()))
                .collect(Collectors.toList());

        return QueryResponse.builder()
                .reply(parsed.reply())
                .agent(agent.name())
                .followUpPrompts(followUps)
                .citations(citations)
                .build();

    }

    private Parsed parseModelJson(String raw) {
        try {
           ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(raw);

            // If your provider wraps completion, adapt here.
            // For now, assume raw is the JSON object itself OR contains it.
            if (root.has("reply")) {
                return new Parsed(
                        root.get("reply").asText(),
                        toStringList(root.get("followUpPrompts"))
                );
            }

            // Fallback: try to extract JSON object substring if provider wraps it in text
            String json = extractFirstJsonObject(raw);
            JsonNode obj = mapper.readTree(json);
            return new Parsed(
                    obj.get("reply").asText(),
                    toStringList(obj.get("followUpPrompts"))
            );
        } catch (Exception e) {
            // Last-resort: treat entire output as reply
            return new Parsed(raw, List.of());
        }
    }

    private List<String> toStringList(JsonNode n) {
        if (n == null || !n.isArray()) return List.of();
        List<String> out = new ArrayList<>();
        n.forEach(x -> out.add(x.asText()));
        return out;
    }

    private String extractFirstJsonObject(String text) {
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) return text.substring(start, end + 1);
        return "{\"reply\":\"" + text.replace("\"", "\\\"") + "\",\"followUpPrompts\":[]}";
    }

    private List<String> ensure3to5(List<String> fromModel, AgentName agent) {
        List<String> cleaned = fromModel == null ? new ArrayList<>() : fromModel.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .collect(Collectors.toCollection(ArrayList::new));

        // Fill with templates until size >= 3
        for (String t : SystemPrompts.buildTemplatePrompt(agent)) {
            if (cleaned.size() >= 3) break;
            if (!cleaned.contains(t)) cleaned.add(t);
        }

        // Cap at 5
        if (cleaned.size() > 5) cleaned = cleaned.subList(0, 5);

        return cleaned;
    }
}
