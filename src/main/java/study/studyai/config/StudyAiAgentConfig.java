package study.studyai.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import study.studyai.config.properties.AiMemoryProperties;

@Configuration
@EnableConfigurationProperties(AiMemoryProperties.class)
public class StudyAiAgentConfig {

    // TODO 后续接入 Spring AI ChatClient、Advisor、VectorStore、ToolCallback 等智能体能力时，统一放在这里配置。
}
