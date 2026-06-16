package study.studyai.studyaiagent.course.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.stereotype.Service;
import study.studyai.common.ErrorCode;
import study.studyai.config.CosClientConfig;
import study.studyai.exception.BusinessException;
import study.studyai.manager.CosManager;
import study.studyai.manager.FileManager;
import study.studyai.studyaiagent.course.model.CourseMarkdownFile;
import study.studyai.studyaiagent.course.service.CourseMarkdownFileService;

import jakarta.annotation.Resource;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
public class CourseMarkdownFileServiceImpl implements CourseMarkdownFileService {

    @Resource
    private CosManager cosManager;

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private FileManager fileManager;

    @Override
    public CourseMarkdownFile uploadMarkdown(String markdown, String fileName) {
        if (StrUtil.isBlank(markdown)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String key = String.format("course/markdown/%s.md", UUID.randomUUID());
        File file = null;
        try {
            file = File.createTempFile("course-markdown-", ".md");
            FileUtil.writeString(markdown, file, StandardCharsets.UTF_8);
            cosManager.putObject(key, file);
            CourseMarkdownFile courseMarkdownFile = new CourseMarkdownFile();
            courseMarkdownFile.setFileKey(key);
            courseMarkdownFile.setFileUrl(getFileUrl(key));
            return courseMarkdownFile;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "Markdown 文件上传失败");
        } finally {
            fileManager.deleteTempFile(file);
        }
    }

    private String getFileUrl(String key) {
        String host = cosClientConfig.getHost();
        if (StrUtil.isBlank(host)) {
            return key;
        }
        return host + "/" + key;
    }
}
