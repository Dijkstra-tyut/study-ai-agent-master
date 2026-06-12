package study.studyai.studyaiagent.memory;

public interface FileMemoryService {

    void appendMessage(Long userId, String conversationId, String role, String content);

    String readMemory(Long userId, String conversationId);
}
