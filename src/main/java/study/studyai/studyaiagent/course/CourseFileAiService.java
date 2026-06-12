package study.studyai.studyaiagent.course;

import org.springframework.web.multipart.MultipartFile;
import study.studyai.model.entity.Course;

public interface CourseFileAiService {

    CourseFileAiResult validateAndAnalyzeCourseFile(Course course, MultipartFile multipartFile, Boolean needChapterAnalysis);
}
