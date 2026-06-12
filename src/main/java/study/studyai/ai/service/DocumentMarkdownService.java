package study.studyai.ai.service;

import org.springframework.web.multipart.MultipartFile;

public interface DocumentMarkdownService {

    String convertToMarkdown(MultipartFile multipartFile);
}
