package com.accenture.ai.onboardingagentgitclient.service;

import com.accenture.ai.onboardingagentgitclient.config.OnboardingProperties;
import com.accenture.ai.onboardingagentgitclient.enums.DocType;
import com.accenture.ai.onboardingagentgitclient.model.AgentAskRequest;
import com.accenture.ai.onboardingagentgitclient.model.AgentAskResponse;
import com.accenture.ai.onboardingagentgitclient.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class AgentDocService {

    private final GitClientUtil gitClientUtil;
    private final WordParser wordParser;
    private final ExcelParser excelParser;
    private final DocumentNormalizer documentNormalizer;
    private final OnboardingProperties properties;

    @Value("${github.models.model}")
    private String model;

    public AgentDocService(GitClientUtil gitClientUtil, WordParser wordParser, ExcelParser excelParser, DocumentNormalizer documentNormalizer, OnboardingProperties properties) {
        this.gitClientUtil = gitClientUtil;
        this.wordParser = wordParser;
        this.excelParser = excelParser;
        this.documentNormalizer = documentNormalizer;
        this.properties = properties;
    }

    public Mono<AgentAskResponse> askDoc(AgentAskRequest req, DocType docType) throws IOException {

        String system = Optional.ofNullable(req.systemInstruction())
                .filter(s -> !s.isBlank())
                .orElse("You are a helpful engineering assistant. Be concise and accurate.");

        String docContext = "";
        if(DocType.WORD.equals(docType)) {
            docContext = parseWord();
        } else if(DocType.EXCEL.equals(docType)) {
            docContext = parseExcel();
        }

        String objective = "You are an assistant that answers user questions using only the provided document source.";
        String instructions = """
Rules:
1) Use ONLY the SOURCE.
2) If not present, say "Not found in source".
3) Keep the plan to 5 bullets max.
""";

        String grounding = """
OBJECTIVE:
%s

INSTRUCTIONS:
%s

SOURCE:
%s
END SOURCE
""".formatted(objective, instructions, docContext);

        Mono<String> planMono = gitClientUtil.callChat(List.of(
                gitClientUtil.msg("system", system),
                gitClientUtil.msg("system", grounding),
                gitClientUtil.msg("user", "Create a short step-by-step plan to answer: " + req.prompt())
        )).map(gitClientUtil::extractText);

        return planMono.flatMap(plan -> gitClientUtil.callChat(List.of(
                                gitClientUtil.msg("system", system),
                                gitClientUtil.msg("user", "User question: " + req.prompt()),
                                gitClientUtil.msg("assistant", "Plan:\n" + plan),
                                gitClientUtil.msg("user", "Now give the final answer. Use the plan, but do not repeat it verbatim.")
                        ))
                        .map(gitClientUtil::extractText)
                        .map(answer -> new AgentAskResponse(model, answer, gitClientUtil.splitSteps(plan)))
        );
    }

    private String parseWord() throws IOException {
        return wordParser.extractText(FileStreamUtil.input(properties.getWordPath()));
    }

    private String parseExcel() throws IOException {
        return documentNormalizer.toLLMContext(
                excelParser.extractSheets(FileStreamUtil.input(properties.getExcelPath())));
    }
}
