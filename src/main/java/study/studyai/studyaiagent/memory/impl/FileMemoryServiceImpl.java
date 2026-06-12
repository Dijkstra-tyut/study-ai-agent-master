package study.studyai.studyaiagent.memory.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.stereotype.Service;
import study.studyai.common.ErrorCode;
import study.studyai.config.properties.AiMemoryProperties;
import study.studyai.exception.BusinessException;
import study.studyai.studyaiagent.memory.FileMemoryService;

import javax.annotation.Resource;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class FileMemoryServiceImpl implements FileMemoryService {

    @Resource
    private AiMemoryProperties aiMemoryProperties;

    @Override
    public void appendMessage(Long userId, String conversationId, String role, String content) {
        if (userId == null || StrUtil.hasBlank(conversationId, role, content)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        File file = getMemoryFile(userId, conversationId);
        FileUtil.appendString(formatMessage(role, content), file, StandardCharsets.UTF_8);
    }

    @Override
    public String readMemory(Long userId, String conversationId) {
        if (userId == null || StrUtil.isBlank(conversationId)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        File file = getMemoryFile(userId, conversationId);
        if (!FileUtil.exist(file)) {
            return "";
        }
        return FileUtil.readString(file, StandardCharsets.UTF_8);
    }

    private File getMemoryFile(Long userId, String conversationId) {
        String fileName = conversationId.replaceAll("[^a-zA-Z0-9_\\-]", "_") + ".md";
        File dir = FileUtil.file(aiMemoryProperties.getFilePath(), String.valueOf(userId));
        FileUtil.mkdir(dir);
        return FileUtil.file(dir, fileName);
    }

    private String formatMessage(String role, String content) {
        return "## " + role + " " + new Date() + "\n\n" + content + "\n\n";
    }
}
