package study.studyai.studyaiagent.memory;

import study.studyai.model.entity.ChatMemoryMessage;

import java.util.List;

public interface ChatMemoryService {

    boolean saveMessage(Long userId, String conversationId, String role, String content);

    List<ChatMemoryMessage> listMessage(Long userId, String conversationId);
}
