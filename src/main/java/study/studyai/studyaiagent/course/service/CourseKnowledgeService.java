package study.studyai.studyaiagent.course.service;

import study.studyai.model.entity.Course;

public interface CourseKnowledgeService {

    String searchCourseContext(Course course, String question);
}
