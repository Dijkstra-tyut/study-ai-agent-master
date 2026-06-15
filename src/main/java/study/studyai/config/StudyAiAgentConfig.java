package study.studyai.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import study.studyai.config.properties.AiMemoryProperties;
import study.studyai.studyaiagent.memory.DatabaseChatMemoryRepository;
import study.studyai.studyaiagent.memory.FileBasedChatMemory;

@Configuration
@EnableConfigurationProperties(AiMemoryProperties.class)
public class StudyAiAgentConfig {

    // TODO 后续接入 Spring AI ChatClient、Advisor、VectorStore、ToolCallback 等智能体能力时，统一放在这里配置。

    @Bean
    public ChatMemory studyDatabaseChatMemory(DatabaseChatMemoryRepository databaseChatMemoryRepository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(databaseChatMemoryRepository)
                .maxMessages(20)
                .build();
    }

    @Bean
    public ChatMemory studyFileChatMemory(AiMemoryProperties aiMemoryProperties) {
        return new FileBasedChatMemory(aiMemoryProperties.getFilePath());
    }
}
