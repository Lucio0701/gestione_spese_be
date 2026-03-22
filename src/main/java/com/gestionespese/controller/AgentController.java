package com.gestionespese.controller;

import com.gestionespese.dto.agent.AgentChatRequest;
import com.gestionespese.dto.agent.AgentChatResponse;
import com.gestionespese.service.AgentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/agent")
public class AgentController {

    private final AgentService agentService;

    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }

    @PostMapping("/chat")
    public ResponseEntity<AgentChatResponse> chat(
            @RequestBody AgentChatRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String response = agentService.chat(userDetails.getUsername(), request.messages());
        return ResponseEntity.ok(new AgentChatResponse(response));
    }
}
