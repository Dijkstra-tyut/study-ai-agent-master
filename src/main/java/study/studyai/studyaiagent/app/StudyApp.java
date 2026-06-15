package study.studyai.studyaiagent.app;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import study.studyai.studyaiagent.memory.StudyConversationId;

@Service
public class StudyApp {

    private static final String SYSTEM_PROMPT = "你是学习智能体系统的学习助手，请根据用户问题给出清晰回答。";

    private final ChatClient chatClient;

    public StudyApp(ChatModel dashscopeChatModel, @Qualifier("studyDatabaseChatMemory") ChatMemory chatMemory) {
        this.chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }

    public String chat(Long userId, String conversationId, String message) {
        String chatId = StudyConversationId.build(userId, conversationId);
        // TODO 后续接入 RAG、工具调用、课程上下文、Advisor 后，在这里统一编排学习智能体流程。
        return chatClient.prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .content();
    }
}
