package study.studyai.studyaiagent.core.impl;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;
import study.studyai.studyaiagent.core.AiChatService;

@Service
public class SpringAiChatServiceImpl implements AiChatService {

    private final ChatClient chatClient;

    public SpringAiChatServiceImpl(ChatModel dashscopeChatModel) {
        this.chatClient = ChatClient.builder(dashscopeChatModel).build();
    }

    @Override
    public String call(String systemPrompt, String userPrompt) {
        return chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .content();
    }

    @Override
    public <T> T callEntity(String systemPrompt, String userPrompt, Class<T> clazz) {
        return chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .entity(clazz);
    }
}