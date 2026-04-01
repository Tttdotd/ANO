package com.tdotd.ano.infrastructure.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tdotd.ano.common.constant.KnowledgeMiningConstants;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Component
public class KnowledgeEmerger {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public KnowledgeEmerger(ChatClient.Builder chatClientBuilder, ObjectMapper objectMapper) {
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    public EmergenceResult emerge(String fromContent, String toContent, String relationType) {
        String prompt = """
                节点A:
                %s

                节点B:
                %s

                关系类型:
                %s
                """.formatted(nullSafe(fromContent), nullSafe(toContent), nullSafe(relationType));
        String raw = chatClient.prompt()
                .system(KnowledgeMiningConstants.EMERGENCE_SYSTEM_PROMPT)
                .user(prompt)
                .call()
                .content();
        try {
            JsonNode root = objectMapper.readTree(raw == null ? "{}" : raw);
            String title = textOrEmpty(root.get("title"));
            String content = textOrEmpty(root.get("content"));
            return new EmergenceResult(title, content);
        } catch (Exception ex) {
            return new EmergenceResult("", "");
        }
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }

    private String textOrEmpty(JsonNode node) {
        if (node == null || node.isNull()) {
            return "";
        }
        return node.asText("");
    }

    public record EmergenceResult(String title, String content) {
    }
}
