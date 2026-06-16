package study.studyai.studyaiagent.course.service;

import study.studyai.studyaiagent.course.model.CourseMarkdownFile;

public interface CourseMarkdownFileService {

    CourseMarkdownFile uploadMarkdown(String markdown, String fileName);
}
