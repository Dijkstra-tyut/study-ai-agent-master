package study.studyai.studyaiagent.course.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import study.studyai.ai.service.AiChatService;
import study.studyai.ai.service.DocumentMarkdownService;
import study.studyai.common.ErrorCode;
import study.studyai.exception.BusinessException;
import study.studyai.model.entity.Course;
import study.studyai.studyaiagent.course.CourseFileAiResult;
import study.studyai.studyaiagent.course.CourseFileAiService;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class CourseFileAiServiceImpl implements CourseFileAiService {

    private static final int MAX_MARKDOWN_LENGTH = 12000;

    @Resource
    private AiChatService aiChatService;

    @Resource
    private DocumentMarkdownService documentMarkdownService;

    @Override
    public CourseFileAiResult validateAndAnalyzeCourseFile(Course course, MultipartFile multipartFile, Boolean needChapterAnalysis) {
        if (course == null || multipartFile == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String markdown = documentMarkdownService.convertToMarkdown(multipartFile);
        validateCourseFileContent(course, markdown);
        CourseFileAiResult courseFileAiResult = new CourseFileAiResult();
        courseFileAiResult.setMarkdown(markdown);
        if (Boolean.TRUE.equals(needChapterAnalysis)) {
            courseFileAiResult.setChapterNameList(analyzeChapterNameList(course, markdown));
        }
        return courseFileAiResult;
    }

    private void validateCourseFileContent(Course course, String markdown) {
        String systemPrompt = "你是学习智能体系统的课程文件审核助手，只判断文件内容是否适合作为该课程资料。请只返回 PASS 或 REJECT，不能输出其他内容。";
        String userPrompt = "课程名称：" + course.getCourse_name()
                + "\n课程介绍：" + StrUtil.blankToDefault(course.getDescription(), "无")
                + "\n文件内容：\n" + limitContent(markdown);
        String result = aiChatService.call(systemPrompt, userPrompt);
        if (!StrUtil.startWithIgnoreCase(StrUtil.trim(result), "PASS")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "课程文件与课程主题不匹配");
        }
    }

    private List<String> analyzeChapterNameList(Course course, String markdown) {
        String systemPrompt = "你是学习智能体系统的课程章节解析助手。请根据课程文件提取大章节标题，不要太细，只返回章节标题列表，每行一个标题，不要解释。";
        String userPrompt = "课程名称：" + course.getCourse_name()
                + "\n课程介绍：" + StrUtil.blankToDefault(course.getDescription(), "无")
                + "\n文件内容：\n" + limitContent(markdown);
        String result = aiChatService.call(systemPrompt, userPrompt);
        return parseChapterNameList(result);
    }

    private String limitContent(String markdown) {
        if (StrUtil.length(markdown) <= MAX_MARKDOWN_LENGTH) {
            return markdown;
        }
        return StrUtil.sub(markdown, 0, MAX_MARKDOWN_LENGTH);
    }

    private List<String> parseChapterNameList(String content) {
        List<String> chapterNameList = new ArrayList<>();
        if (StrUtil.isBlank(content)) {
            return chapterNameList;
        }
        String[] lines = content.replace("\r", "\n").split("\n");
        for (String line : lines) {
            String chapterName = normalizeChapterName(line);
            if (StrUtil.isBlank(chapterName) || chapterNameList.contains(chapterName)) {
                continue;
            }
            chapterNameList.add(chapterName);
        }
        return CollUtil.sub(chapterNameList, 0, Math.min(chapterNameList.size(), 20));
    }

    private String normalizeChapterName(String line) {
        String chapterName = StrUtil.trim(line);
        chapterName = chapterName.replaceAll("^[#\\-\\*\\d\\.、\\s]+", "");
        return StrUtil.trim(chapterName);
    }
}
