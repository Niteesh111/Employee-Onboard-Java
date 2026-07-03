package com.accenture.ai.onboardingagentgitclient.controller;

import com.accenture.ai.onboardingagentgitclient.enums.DocType;
import com.accenture.ai.onboardingagentgitclient.model.AgentAskRequest;
import com.accenture.ai.onboardingagentgitclient.model.AgentAskResponse;
import com.accenture.ai.onboardingagentgitclient.model.PromptRequest;
import com.accenture.ai.onboardingagentgitclient.model.QueryResponse;
import com.accenture.ai.onboardingagentgitclient.service.AgentDocService;
import com.accenture.ai.onboardingagentgitclient.service.AgentOrchestratorService;
import com.accenture.ai.onboardingagentgitclient.service.AgentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.io.IOException;

@RestController
@RequestMapping("/api/onboard")
public class AgentController {

    private final AgentService agentService;
    private final AgentDocService agentDocService;
    private final AgentOrchestratorService agentOrchestratorService;

    public AgentController(AgentService agentService, AgentDocService agentDocService,
                           AgentOrchestratorService agentOrchestratorService) {
        this.agentService = agentService;
        this.agentDocService = agentDocService;
        this.agentOrchestratorService = agentOrchestratorService;
    }

    @PostMapping("/agent/ask")
    public Mono<AgentAskResponse> ask(@Valid @RequestBody AgentAskRequest req) {
        return agentService.ask(req);
    }

    @PostMapping("/agent/askWord")
    public Mono<AgentAskResponse> askWord(@Valid @RequestBody AgentAskRequest req) throws IOException {
        return agentDocService.askDoc(req, DocType.WORD);
    }

    @PostMapping("/agent/askExcel")
    public Mono<AgentAskResponse> askExcel(@Valid @RequestBody AgentAskRequest req) throws IOException {
        return agentDocService.askDoc(req, DocType.EXCEL);
    }

    @PostMapping("/query")
    public ResponseEntity<QueryResponse> query(@Valid @RequestBody PromptRequest request) {
        try {
            QueryResponse res = agentOrchestratorService.handle(request);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }
}
