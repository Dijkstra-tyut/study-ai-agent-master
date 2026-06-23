package study.studyai.studyaiagent.course.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import study.studyai.common.ErrorCode;
import study.studyai.exception.BusinessException;
import study.studyai.model.entity.Course;
import study.studyai.studyaiagent.core.AiChatService;
import study.studyai.studyaiagent.core.DocumentMarkdownService;
import study.studyai.studyaiagent.course.model.CourseChapterAnalysisResult;
import study.studyai.studyaiagent.course.model.CourseFileCheckResult;
import study.studyai.studyaiagent.course.service.CourseFileAiService;

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
            throw new BusinessException(ErrorCode.PARAMS_ERROR, StrUtil.blankToDefault(reason, "Course file does not match the course topic"));
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
            throw new BusinessException(ErrorCode.PARAMS_ERROR, StrUtil.blankToDefault(reason, "No reliable chapter outline found in the file"));
        }
        List<String> chapterNameList = normalizeChapterNameList(analysisResult.getChapterNameList());
        if (CollUtil.isEmpty(chapterNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "No reliable chapter outline found in the file");
        }
        return CollUtil.sub(chapterNameList, 0, Math.min(chapterNameList.size(), MAX_CHAPTER_SIZE));
    }

    private CourseFileCheckResult checkCourseFile(Course course, String markdown) {
        String systemPrompt = "You are a course material relevance checker. "
                + "Judge only whether the extracted file text is related to the course name and description. "
                + "Pass if the material is a textbook excerpt, chapter outline, lecture note, syllabus, or course reference related to the course. "
                + "Do not reject a file merely because it is short, an excerpt, a sample, or lacks exercises/code examples. "
                + "Reject only when the text is empty, unreadable, or clearly unrelated to the course. "
                + "Return a JSON object with fields: pass(boolean), reason(string).";
        String userPrompt = "Course name: " + course.getCourse_name()
                + "\nCourse description: " + StrUtil.blankToDefault(course.getDescription(), "none")
                + "\nExtracted file text:\n" + limitContent(markdown, CHECK_CONTENT_LENGTH);
        return aiChatService.callEntity(systemPrompt, userPrompt, CourseFileCheckResult.class);
    }

    private CourseChapterAnalysisResult analyzeChapter(Course course, String markdown) {
        String systemPrompt = "You are a course chapter outline extractor. "
                + "Use the extracted file text to decide whether it contains a table of contents, chapter headings, or obvious major sections. "
                + "If major sections can be identified, return hasDirectory=true and chapterNameList with only major chapter titles. "
                + "If no reliable sections exist, return hasDirectory=false and explain briefly. "
                + "Return a JSON object with fields: hasDirectory(boolean), reason(string), chapterNameList(array of strings).";
        String userPrompt = "Course name: " + course.getCourse_name()
                + "\nCourse description: " + StrUtil.blankToDefault(course.getDescription(), "none")
                + "\nExtracted file text:\n" + limitContent(markdown, CHAPTER_CONTENT_LENGTH);
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
