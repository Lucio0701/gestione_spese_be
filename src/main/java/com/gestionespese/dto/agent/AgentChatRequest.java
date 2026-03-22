package com.gestionespese.dto.agent;

import java.util.List;

public record AgentChatRequest(List<AgentMessage> messages) {}
