package study.studyai.studyaiagent.app;

import com.alibaba.dashscope.common.Role;
import org.springframework.stereotype.Service;
import study.studyai.ai.service.AiChatService;
import study.studyai.studyaiagent.memory.ChatMemoryService;
import study.studyai.studyaiagent.memory.FileMemoryService;

import javax.annotation.Resource;

@Service
public class StudyApp {

    @Resource
    private AiChatService aiChatService;

    @Resource
    private ChatMemoryService chatMemoryService;

    @Resource
    private FileMemoryService fileMemoryService;

    public String chat(Long userId, String conversationId, String message) {
        chatMemoryService.saveMessage(userId, conversationId, Role.USER.getValue(), message);
        fileMemoryService.appendMessage(userId, conversationId, Role.USER.getValue(), message);
        // TODO 后续接入 RAG、工具调用、课程上下文、Advisor 后，在这里统一编排学习智能体流程。
        String answer = aiChatService.call("你是学习智能体系统的学习助手，请根据用户问题给出清晰回答。", message);
        chatMemoryService.saveMessage(userId, conversationId, Role.ASSISTANT.getValue(), answer);
        fileMemoryService.appendMessage(userId, conversationId, Role.ASSISTANT.getValue(), answer);
        return answer;
    }
}
