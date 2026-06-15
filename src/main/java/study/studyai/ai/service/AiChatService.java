package study.studyai.ai.service;

public interface AiChatService {

    String call(String systemPrompt, String userPrompt);

    <T> T callEntity(String systemPrompt, String userPrompt, Class<T> clazz);
}
