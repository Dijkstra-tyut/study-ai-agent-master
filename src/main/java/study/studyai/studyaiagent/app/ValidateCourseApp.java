package study.studyai.studyaiagent.app;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import study.studyai.model.entity.Course;
import study.studyai.studyaiagent.course.CourseFileAiResult;
import study.studyai.studyaiagent.course.CourseFileAiService;

import javax.annotation.Resource;

@Service
public class ValidateCourseApp {

    @Resource
    private CourseFileAiService courseFileAiService;

    public CourseFileAiResult validateAndAnalyze(Course course, MultipartFile multipartFile, Boolean needChapterAnalysis) {
        return courseFileAiService.validateAndAnalyzeCourseFile(course, multipartFile, needChapterAnalysis);
    }
}
