package study.studyai.studyaiagent.memory.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;
import study.studyai.common.ErrorCode;
import study.studyai.exception.BusinessException;
import study.studyai.mapper.ChatMemoryMessageMapper;
import study.studyai.model.entity.ChatMemoryMessage;
import study.studyai.studyaiagent.memory.ChatMemoryService;

import javax.annotation.Resource;
import java.util.List;

@Service
public class ChatMemoryServiceImpl implements ChatMemoryService {

    @Resource
    private ChatMemoryMessageMapper chatMemoryMessageMapper;

    @Override
    public boolean saveMessage(Long userId, String conversationId, String role, String content) {
        if (userId == null || StrUtil.hasBlank(conversationId, role, content)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        ChatMemoryMessage chatMemoryMessage = new ChatMemoryMessage();
        chatMemoryMessage.setUser_id(userId);
        chatMemoryMessage.setConversation_id(conversationId);
        chatMemoryMessage.setRole(role);
        chatMemoryMessage.setContent(content);
        return chatMemoryMessageMapper.insert(chatMemoryMessage) > 0;
    }

    @Override
    public List<ChatMemoryMessage> listMessage(Long userId, String conversationId) {
        if (userId == null || StrUtil.isBlank(conversationId)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<ChatMemoryMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.eq("conversation_id", conversationId);
        queryWrapper.orderByAsc("id");
        return chatMemoryMessageMapper.selectList(queryWrapper);
    }
}
