package study.studyai.studyaiagent.core.impl;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import study.studyai.common.ErrorCode;
import study.studyai.exception.BusinessException;
import study.studyai.studyaiagent.core.AiChatService;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class SpringAiChatServiceImpl implements AiChatService {

    private static final String COMPATIBLE_CHAT_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";

    private final RestClient restClient;

    private final ObjectMapper objectMapper;

    @Value("${spring.ai.dashscope.api-key}")
    private String apiKey;

    @Value("${spring.ai.dashscope.chat.options.model:qwen3.6-plus}")
    private String model;

    public SpringAiChatServiceImpl(RestClient.Builder restClientBuilder, ObjectMapper objectMapper) {
        this.restClient = restClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    @Override
    public String call(String systemPrompt, String userPrompt) {
        return callCompatible(systemPrompt, userPrompt);
    }

    @Override
    public <T> T callEntity(String systemPrompt, String userPrompt, Class<T> clazz) {
        String jsonPrompt = userPrompt + "\n\nReturn only one valid JSON object. Do not use Markdown code fences. Do not output explanations.";
        String content = callCompatible(systemPrompt, jsonPrompt);
        try {
            return objectMapper.readValue(extractJsonObject(content), clazz);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI response JSON parse failed");
        }
    }

    private String callCompatible(String systemPrompt, String userPrompt) {
        if (StrUtil.isBlank(apiKey)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "DashScope API key is not configured");
        }
        try {
            ChatRequest request = new ChatRequest(
                    model,
                    List.of(
                            Map.of("role", "system", "content", StrUtil.blankToDefault(systemPrompt, "")),
                            Map.of("role", "user", "content", StrUtil.blankToDefault(userPrompt, ""))
                    )
            );
            ChatResponse response = restClient.post()
                    .uri(COMPATIBLE_CHAT_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + apiKey)
                    .body(request)
                    .retrieve()
                    .body(ChatResponse.class);
            if (response == null || response.choices == null || response.choices.isEmpty()
                    || response.choices.getFirst().message == null
                    || StrUtil.isBlank(response.choices.getFirst().message.content)) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI response content is empty");
            }
            log.info("DashScope compatible chat succeeded, id={}, model={}, promptTokens={}, completionTokens={}, totalTokens={}",
                    response.id, response.model,
                    response.usage == null ? null : response.usage.prompt_tokens,
                    response.usage == null ? null : response.usage.completion_tokens,
                    response.usage == null ? null : response.usage.total_tokens);
            return response.choices.getFirst().message.content;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI call failed");
        }
    }

    private String extractJsonObject(String content) throws Exception {
        String text = StrUtil.trim(content);
        if (text.startsWith("```")) {
            text = text.replaceFirst("^```[a-zA-Z]*", "");
            text = text.replaceFirst("```$", "");
            text = StrUtil.trim(text);
        }
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start < 0 || end < start) {
            throw new IllegalArgumentException("No JSON object found");
        }
        String json = text.substring(start, end + 1);
        JsonNode jsonNode = objectMapper.readTree(json);
        return objectMapper.writeValueAsString(jsonNode);
    }

    private record ChatRequest(String model, List<Map<String, String>> messages) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class ChatResponse {
        public String id;
        public String model;
        public List<Choice> choices;
        public Usage usage;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Choice {
        public Message message;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Message {
        public String content;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Usage {
        public Integer prompt_tokens;
        public Integer completion_tokens;
        public Integer total_tokens;
    }
}
