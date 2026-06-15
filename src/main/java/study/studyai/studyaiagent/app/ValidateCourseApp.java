package study.studyai.studyaiagent.app;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import study.studyai.model.entity.Course;
import study.studyai.studyaiagent.course.CourseFileAiService;

import jakarta.annotation.Resource;
import java.util.List;

@Service
public class ValidateCourseApp {

    @Resource
    private CourseFileAiService courseFileAiService;

    public String convertToMarkdown(MultipartFile multipartFile) {
        return courseFileAiService.convertToMarkdown(multipartFile);
    }

    public void validateCourseFile(Course course, String markdown) {
        courseFileAiService.validateCourseFile(course, markdown);
    }

    public List<String> analyzeChapterNameList(Course course, String markdown) {
        return courseFileAiService.analyzeChapterNameList(course, markdown);
    }
}