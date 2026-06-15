package study.studyai.studyaiagent.memory;

import cn.hutool.core.util.StrUtil;
import study.studyai.common.ErrorCode;
import study.studyai.exception.BusinessException;

public class StudyConversationId {

    private static final String SEPARATOR = ":";

    private StudyConversationId() {
    }

    public static String build(Long userId, String conversationId) {
        if (userId == null || StrUtil.isBlank(conversationId)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return userId + SEPARATOR + conversationId;
    }

    public static Long getUserId(String conversationId) {
        String[] parts = split(conversationId);
        return Long.valueOf(parts[0]);
    }

    public static String getRealConversationId(String conversationId) {
        String[] parts = split(conversationId);
        return parts[1];
    }

    private static String[] split(String conversationId) {
        if (StrUtil.isBlank(conversationId) || !conversationId.contains(SEPARATOR)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "对话ID格式错误");
        }
        String[] parts = conversationId.split(SEPARATOR, 2);
        if (parts.length != 2 || StrUtil.hasBlank(parts[0], parts[1])) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "对话ID格式错误");
        }
        return parts;
    }
}
