package com.accenture.ai.onboardingagentgitclient.service;

import com.accenture.ai.onboardingagentgitclient.model.AgentAskRequest;
import com.accenture.ai.onboardingagentgitclient.model.AgentAskResponse;
import com.accenture.ai.onboardingagentgitclient.util.GitClientUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.*;


@Service
public class AgentService {

    private final GitClientUtil gitClientUtil;

    @Value("${github.models.model}")
    private String model;

    public AgentService(GitClientUtil gitClientUtil) {
        this.gitClientUtil = gitClientUtil;
    }

    public Mono<AgentAskResponse> ask(AgentAskRequest req) {
        String system = Optional.ofNullable(req.systemInstruction())
                .filter(s -> !s.isBlank())
                .orElse("You are a helpful engineering assistant. Be concise and accurate.");

        // Step 1: planning (kept short)
        Mono<String> planMono = gitClientUtil.callChat(List.of(
                gitClientUtil.msg("system", system),
                gitClientUtil.msg("user", "Create a short step-by-step plan (max 5 bullets) to answer: " + req.prompt())
        )).map(gitClientUtil::extractText);

        // Step 2: final answer using the plan
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
}
