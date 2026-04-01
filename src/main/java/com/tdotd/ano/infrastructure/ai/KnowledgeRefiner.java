package com.tdotd.ano.infrastructure.ai;

import com.tdotd.ano.common.constant.KnowledgeArchiveConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KnowledgeRefiner {

    private final ChatClient chatClient;

    public KnowledgeRefiner(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public String refine(String title, String description, String noteContent) {
        long start = System.currentTimeMillis();
        String prompt = """
                任务标题:
                %s

                任务描述:
                %s

                任务笔记:
                %s
                """.formatted(nullSafe(title), nullSafe(description), nullSafe(noteContent));
        String result = chatClient.prompt()
                .system(KnowledgeArchiveConstants.CHAT_SYSTEM_PROMPT)
                .user(prompt)
                .call()
                .content();
        log.info("knowledge refine finished: costMs={}", System.currentTimeMillis() - start);
        return result == null ? "" : result.trim();
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }
}
