package study.studyai.studyaiagent.memory;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Repository;
import study.studyai.mapper.ChatMemoryMessageMapper;
import study.studyai.model.entity.ChatMemoryMessage;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class DatabaseChatMemoryRepository implements ChatMemoryRepository {

    @Resource
    private ChatMemoryMessageMapper chatMemoryMessageMapper;

    @Override
    public List<String> findConversationIds() {
        QueryWrapper<ChatMemoryMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("DISTINCT user_id, conversation_id");
        List<ChatMemoryMessage> messageList = chatMemoryMessageMapper.selectList(queryWrapper);
        return messageList.stream()
                .map(message -> StudyConversationId.build(message.getUser_id(), message.getConversation_id()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Message> findByConversationId(String conversationId) {
        Long userId = StudyConversationId.getUserId(conversationId);
        String realConversationId = StudyConversationId.getRealConversationId(conversationId);
        QueryWrapper<ChatMemoryMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.eq("conversation_id", realConversationId);
        queryWrapper.orderByAsc("message_order");
        queryWrapper.orderByAsc("id");
        List<ChatMemoryMessage> messageList = chatMemoryMessageMapper.selectList(queryWrapper);
        if (CollUtil.isEmpty(messageList)) {
            return new ArrayList<>();
        }
        return messageList.stream().map(this::toMessage).collect(Collectors.toList());
    }

    @Override
    public void saveAll(String conversationId, List<Message> messages) {
        Long userId = StudyConversationId.getUserId(conversationId);
        String realConversationId = StudyConversationId.getRealConversationId(conversationId);
        this.deleteByConversationId(conversationId);
        for (int i = 0; i < messages.size(); i++) {
            ChatMemoryMessage chatMemoryMessage = toChatMemoryMessage(userId, realConversationId, messages.get(i), i);
            chatMemoryMessageMapper.insert(chatMemoryMessage);
        }
    }

    @Override
    public void deleteByConversationId(String conversationId) {
        Long userId = StudyConversationId.getUserId(conversationId);
        String realConversationId = StudyConversationId.getRealConversationId(conversationId);
        QueryWrapper<ChatMemoryMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.eq("conversation_id", realConversationId);
        chatMemoryMessageMapper.delete(queryWrapper);
    }

    private ChatMemoryMessage toChatMemoryMessage(Long userId, String conversationId, Message message, Integer messageOrder) {
        ChatMemoryMessage chatMemoryMessage = new ChatMemoryMessage();
        chatMemoryMessage.setUser_id(userId);
        chatMemoryMessage.setConversation_id(conversationId);
        chatMemoryMessage.setRole(message.getMessageType().getValue());
        chatMemoryMessage.setContent(message.getText());
        chatMemoryMessage.setMessage_order(messageOrder);
        return chatMemoryMessage;
    }

    private Message toMessage(ChatMemoryMessage chatMemoryMessage) {
        MessageType messageType = MessageType.fromValue(chatMemoryMessage.getRole());
        if (MessageType.ASSISTANT.equals(messageType)) {
            return new AssistantMessage(chatMemoryMessage.getContent());
        }
        if (MessageType.SYSTEM.equals(messageType)) {
            return new SystemMessage(chatMemoryMessage.getContent());
        }
        return new UserMessage(chatMemoryMessage.getContent());
    }
}
