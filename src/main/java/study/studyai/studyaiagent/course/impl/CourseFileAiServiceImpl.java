package study.studyai.studyaiagent.course.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import study.studyai.common.ErrorCode;
import study.studyai.exception.BusinessException;
import study.studyai.model.entity.Course;
import study.studyai.studyaiagent.core.AiChatService;
import study.studyai.studyaiagent.core.DocumentMarkdownService;
import study.studyai.studyaiagent.course.CourseChapterAnalysisResult;
import study.studyai.studyaiagent.course.CourseFileAiService;
import study.studyai.studyaiagent.course.CourseFileCheckResult;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class CourseFileAiServiceImpl implements CourseFileAiService {

    private static final int CHECK_CONTENT_LENGTH = 8000;

    private static final int CHAPTER_CONTENT_LENGTH = 16000;

    private static final int MAX_CHAPTER_SIZE = 20;

    @Resource
    private AiChatService aiChatService;

    @Resource
    private DocumentMarkdownService documentMarkdownService;

    @Override
    public String convertToMarkdown(MultipartFile multipartFile) {
        return documentMarkdownService.convertToMarkdown(multipartFile);
    }

    @Override
    public void validateCourseFile(Course course, String markdown) {
        if (course == null || StrUtil.isBlank(markdown)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        CourseFileCheckResult checkResult = checkCourseFile(course, markdown);
        if (checkResult == null || !Boolean.TRUE.equals(checkResult.getPass())) {
            String reason = checkResult == null ? null : checkResult.getReason();
            throw new BusinessException(ErrorCode.PARAMS_ERROR, StrUtil.blankToDefault(reason, "课程文件与课程主题不匹配"));
        }
    }

    @Override
    public List<String> analyzeChapterNameList(Course course, String markdown) {
        if (course == null || StrUtil.isBlank(markdown)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        CourseChapterAnalysisResult analysisResult = analyzeChapter(course, markdown);
        if (analysisResult == null || !Boolean.TRUE.equals(analysisResult.getHasDirectory())) {
            String reason = analysisResult == null ? null : analysisResult.getReason();
            throw new BusinessException(ErrorCode.PARAMS_ERROR, StrUtil.blankToDefault(reason, "文件没有目录，无法进行章节切分"));
        }
        List<String> chapterNameList = normalizeChapterNameList(analysisResult.getChapterNameList());
        if (CollUtil.isEmpty(chapterNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件没有目录，无法进行章节切分");
        }
        return CollUtil.sub(chapterNameList, 0, Math.min(chapterNameList.size(), MAX_CHAPTER_SIZE));
    }

    private CourseFileCheckResult checkCourseFile(Course course, String markdown) {
        String systemPrompt = "你是学习智能体系统的课程文件审核助手。只根据用户提供的文件开头内容判断该文件是否适合作为课程资料。"
                + "请按结构化对象返回：pass 表示是否通过，reason 表示原因。不要进行章节切分。";
        String userPrompt = "课程名称：" + course.getCourse_name()
                + "\n课程介绍：" + StrUtil.blankToDefault(course.getDescription(), "无")
                + "\n文件开头内容：\n" + limitContent(markdown, CHECK_CONTENT_LENGTH);
        return aiChatService.callEntity(systemPrompt, userPrompt, CourseFileCheckResult.class);
    }

    private CourseChapterAnalysisResult analyzeChapter(Course course, String markdown) {
        String systemPrompt = "你是学习智能体系统的课程章节解析助手。只根据用户提供的文件开头内容判断是否存在目录或明显大章节。"
                + "如果没有目录或无法可靠切分，hasDirectory 返回 false，并说明原因。"
                + "如果可以切分，hasDirectory 返回 true，chapterNameList 只返回大章节标题，不要提取过细小节。";
        String userPrompt = "课程名称：" + course.getCourse_name()
                + "\n课程介绍：" + StrUtil.blankToDefault(course.getDescription(), "无")
                + "\n文件开头内容：\n" + limitContent(markdown, CHAPTER_CONTENT_LENGTH);
        return aiChatService.callEntity(systemPrompt, userPrompt, CourseChapterAnalysisResult.class);
    }

    private String limitContent(String markdown, int maxLength) {
        if (StrUtil.length(markdown) <= maxLength) {
            return markdown;
        }
        return StrUtil.sub(markdown, 0, maxLength);
    }

    private List<String> normalizeChapterNameList(List<String> chapterNameList) {
        List<String> resultList = new ArrayList<>();
        if (CollUtil.isEmpty(chapterNameList)) {
            return resultList;
        }
        for (String chapterName : chapterNameList) {
            String text = StrUtil.trim(chapterName);
            if (StrUtil.isBlank(text) || resultList.contains(text)) {
                continue;
            }
            resultList.add(text);
        }
        return resultList;
    }
}