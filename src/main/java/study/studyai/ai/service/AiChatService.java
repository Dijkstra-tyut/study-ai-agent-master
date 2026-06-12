package study.studyai.ai.service;

import study.studyai.ai.model.AiChatMessage;

import java.util.List;

public interface AiChatService {

    String call(String systemPrompt, String userPrompt);

    String call(List<AiChatMessage> messages);
}
