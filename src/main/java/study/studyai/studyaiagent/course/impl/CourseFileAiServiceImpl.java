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
import study.studyai.studyaiagent.course.CourseFileCheckResult;

import jakarta.annotation.Resource;

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
        CourseFileCheckResult checkResult = checkCourseFile(course, markdown, needChapterAnalysis);
        if (!Boolean.TRUE.equals(checkResult.getPass())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, StrUtil.blankToDefault(checkResult.getReason(), "课程文件与课程主题不匹配"));
        }
        CourseFileAiResult courseFileAiResult = new CourseFileAiResult();
        courseFileAiResult.setMarkdown(markdown);
        if (Boolean.TRUE.equals(needChapterAnalysis) && checkResult.getChapterNameList() != null) {
            courseFileAiResult.setChapterNameList(CollUtil.sub(checkResult.getChapterNameList(), 0, Math.min(checkResult.getChapterNameList().size(), 20)));
        }
        return courseFileAiResult;
    }

    private CourseFileCheckResult checkCourseFile(Course course, String markdown, Boolean needChapterAnalysis) {
        String systemPrompt = "你是学习智能体系统的课程文件审核助手。请判断文件内容是否适合作为该课程资料，并按结构化对象返回结果。"
                + "pass 表示是否通过审核，reason 表示原因。needChapterAnalysis 为 true 时，chapterNameList 只提取大章节标题，不要太细。";
        String userPrompt = "课程名称：" + course.getCourse_name()
                + "\n课程介绍：" + StrUtil.blankToDefault(course.getDescription(), "无")
                + "\n是否需要章节解析：" + Boolean.TRUE.equals(needChapterAnalysis)
                + "\n文件内容：\n" + limitContent(markdown);
        return aiChatService.callEntity(systemPrompt, userPrompt, CourseFileCheckResult.class);
    }

    private String limitContent(String markdown) {
        if (StrUtil.length(markdown) <= MAX_MARKDOWN_LENGTH) {
            return markdown;
        }
        return StrUtil.sub(markdown, 0, MAX_MARKDOWN_LENGTH);
    }
}
