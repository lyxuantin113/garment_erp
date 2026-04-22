package com.garment.erp.agent.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class ErpAgentService {

    private final ChatClient chatClient;

    public ErpAgentService(
            ChatClient.Builder builder,
            @Value("file:AGENTS.md") Resource systemPromptResource) throws Exception {

        // Đọc System Prompt từ file AGENTS.md ở cấp root của project
        String systemPrompt = new String(systemPromptResource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        // Khởi tạo ChatClient: Nhồi System Prompt + Default Function (Tool)
        this.chatClient = builder
                .defaultSystem(systemPrompt)
                .defaultFunctions("queryGraph") // Mapping trực tiếp với tên bean trong GraphAgentTools
                .build();
    }

    public String chat(String userMessage) {
        return chatClient.prompt()
                .user(userMessage)
                .call()
                .content();
    }
}
