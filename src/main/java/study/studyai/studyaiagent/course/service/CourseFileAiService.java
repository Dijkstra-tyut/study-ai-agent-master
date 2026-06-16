package study.studyai.studyaiagent.course.service;

import org.springframework.web.multipart.MultipartFile;
import study.studyai.model.entity.Course;

import java.util.List;

public interface CourseFileAiService {

    String convertToMarkdown(MultipartFile multipartFile);

    void validateCourseFile(Course course, String markdown);

    List<String> analyzeChapterNameList(Course course, String markdown);
}
