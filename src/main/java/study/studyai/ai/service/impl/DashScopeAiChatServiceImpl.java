package study.studyai.ai.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import org.springframework.stereotype.Service;
import study.studyai.ai.model.AiChatMessage;
import study.studyai.ai.service.AiChatService;
import study.studyai.common.ErrorCode;
import study.studyai.config.properties.DashScopeAiProperties;
import study.studyai.exception.BusinessException;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class DashScopeAiChatServiceImpl implements AiChatService {

    @Resource
    private Generation generation;

    @Resource
    private DashScopeAiProperties dashScopeAiProperties;

    @Override
    public String call(String systemPrompt, String userPrompt) {
        List<AiChatMessage> messages = new ArrayList<>();
        if (StrUtil.isNotBlank(systemPrompt)) {
            messages.add(new AiChatMessage(Role.SYSTEM.getValue(), systemPrompt));
        }
        messages.add(new AiChatMessage(Role.USER.getValue(), userPrompt));
        return this.call(messages);
    }

    @Override
    public String call(List<AiChatMessage> messages) {
        if (CollUtil.isEmpty(messages)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (StrUtil.isBlank(dashScopeAiProperties.getApiKey())) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "DashScope API Key 未配置");
        }
        try {
            List<Message> dashScopeMessages = new ArrayList<>();
            for (AiChatMessage aiChatMessage : messages) {
                dashScopeMessages.add(Message.builder()
                        .role(aiChatMessage.getRole())
                        .content(aiChatMessage.getContent())
                        .build());
            }
            GenerationParam param = GenerationParam.builder()
                    .apiKey(dashScopeAiProperties.getApiKey())
                    .model(dashScopeAiProperties.getChat().getOptions().getModel())
                    .messages(dashScopeMessages)
                    .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                    .build();
            GenerationResult result = generation.call(param);
            if (result == null || result.getOutput() == null || CollUtil.isEmpty(result.getOutput().getChoices())) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "大模型返回为空");
            }
            Message message = result.getOutput().getChoices().get(0).getMessage();
            if (message == null || StrUtil.isBlank(message.getContent())) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "大模型返回为空");
            }
            return message.getContent();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "大模型调用失败");
        }
    }
}
