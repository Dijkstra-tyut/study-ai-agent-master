package study.studyai.studyaiagent.core;

import org.springframework.web.multipart.MultipartFile;

public interface DocumentMarkdownService {

    String convertToMarkdown(MultipartFile multipartFile);
}
