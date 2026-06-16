package study.studyai.studyaiagent.course.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import study.studyai.common.ErrorCode;
import study.studyai.exception.BusinessException;
import study.studyai.mapper.CourseFileMapper;
import study.studyai.model.entity.Course;
import study.studyai.model.entity.CourseFile;
import study.studyai.service.FileService;
import study.studyai.studyaiagent.course.service.CourseKnowledgeService;

import jakarta.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class CourseKnowledgeServiceImpl implements CourseKnowledgeService {

    private static final int MAX_FILE_CONTENT_LENGTH = 30000;

    private static final int CHUNK_SIZE = 2000;

    private static final int CHUNK_OVERLAP = 200;

    private static final int MAX_CONTEXT_LENGTH = 10000;

    private static final int MAX_CHUNK_COUNT = 5;

    @Resource
    private CourseFileMapper courseFileMapper;

    @Resource
    private FileService fileService;

    @Autowired(required = false)
    private EmbeddingModel embeddingModel;

    @Override
    public String searchCourseContext(Course course, String question) {
        if (course == null || course.getCourse_id() == null || StrUtil.isBlank(question)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<CourseFile> courseFileList = listCourseMarkdownFile(course.getCourse_id());
        if (CollUtil.isEmpty(courseFileList)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "课程暂无可用知识库文件");
        }
        List<CourseKnowledgeChunk> chunkList = buildChunkList(courseFileList);
        if (CollUtil.isEmpty(chunkList)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "课程知识库内容为空");
        }
        List<CourseKnowledgeChunk> selectedChunkList = searchBySpringAiVectorStore(chunkList, question);
        if (CollUtil.isEmpty(selectedChunkList)) {
            selectedChunkList = selectChunkList(chunkList, question);
        }
        return buildContext(selectedChunkList);
    }

    private List<CourseFile> listCourseMarkdownFile(Long courseId) {
        QueryWrapper<CourseFile> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("course_id", courseId);
        queryWrapper.isNotNull("markdown_key");
        queryWrapper.orderByDesc("id");
        return courseFileMapper.selectList(queryWrapper);
    }

    private List<CourseKnowledgeChunk> buildChunkList(List<CourseFile> courseFileList) {
        List<CourseKnowledgeChunk> chunkList = new ArrayList<>();
        for (CourseFile courseFile : courseFileList) {
            String markdown = readMarkdown(courseFile);
            if (StrUtil.isBlank(markdown)) {
                continue;
            }
            markdown = limitContent(markdown, MAX_FILE_CONTENT_LENGTH);
            int start = 0;
            while (start < markdown.length()) {
                int end = Math.min(start + CHUNK_SIZE, markdown.length());
                String content = markdown.substring(start, end);
                if (StrUtil.isNotBlank(content)) {
                    chunkList.add(new CourseKnowledgeChunk(courseFile.getFile_name(), content));
                }
                if (end >= markdown.length()) {
                    break;
                }
                start = Math.max(0, end - CHUNK_OVERLAP);
            }
        }
        return chunkList;
    }

    private String readMarkdown(CourseFile courseFile) {
        if (courseFile == null || StrUtil.isBlank(courseFile.getMarkdown_key())) {
            return null;
        }
        COSObject cosObject = fileService.downloadFile(courseFile.getMarkdown_key());
        try (COSObjectInputStream inputStream = cosObject.getObjectContent()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "读取课程知识库失败");
        }
    }

    private List<CourseKnowledgeChunk> selectChunkList(List<CourseKnowledgeChunk> chunkList, String question) {
        for (CourseKnowledgeChunk chunk : chunkList) {
            chunk.setScore(calcScore(chunk.getContent(), question));
        }
        List<CourseKnowledgeChunk> selectedChunkList = chunkList.stream()
                .sorted(Comparator.comparing(CourseKnowledgeChunk::getScore).reversed())
                .limit(MAX_CHUNK_COUNT)
                .toList();
        if (selectedChunkList.stream().allMatch(chunk -> chunk.getScore() <= 0)) {
            return CollUtil.sub(chunkList, 0, Math.min(chunkList.size(), MAX_CHUNK_COUNT));
        }
        return selectedChunkList;
    }

    private List<CourseKnowledgeChunk> searchBySpringAiVectorStore(List<CourseKnowledgeChunk> chunkList, String question) {
        if (embeddingModel == null) {
            return new ArrayList<>();
        }
        try {
            VectorStore vectorStore = SimpleVectorStore.builder(embeddingModel).build();
            vectorStore.add(chunkList.stream()
                    .map(chunk -> new Document(chunk.getContent(), Map.of("fileName", chunk.getFileName())))
                    .toList());
            SearchRequest searchRequest = SearchRequest.builder()
                    .query(question)
                    .topK(MAX_CHUNK_COUNT)
                    .similarityThresholdAll()
                    .build();
            return vectorStore.similaritySearch(searchRequest).stream()
                    .map(document -> new CourseKnowledgeChunk(String.valueOf(document.getMetadata().get("fileName")), document.getText()))
                    .toList();
        } catch (Exception e) {
            // 向量检索依赖 DashScope Embedding，失败时退回本地片段检索，保证问答链路可用。
            return new ArrayList<>();
        }
    }

    private int calcScore(String content, String question) {
        int score = 0;
        String lowerContent = StrUtil.nullToEmpty(content).toLowerCase();
        Set<Character> charSet = new HashSet<>();
        for (char ch : StrUtil.nullToEmpty(question).toLowerCase().toCharArray()) {
            if (!Character.isWhitespace(ch) && Character.isLetterOrDigit(ch)) {
                charSet.add(ch);
            }
        }
        for (Character ch : charSet) {
            if (lowerContent.indexOf(ch) >= 0) {
                score++;
            }
        }
        String[] wordArray = StrUtil.nullToEmpty(question).toLowerCase().split("[^a-z0-9\\u4e00-\\u9fa5]+");
        for (String word : wordArray) {
            if (StrUtil.length(word) > 1 && lowerContent.contains(word)) {
                score += word.length() * 3;
            }
        }
        return score;
    }

    private String buildContext(List<CourseKnowledgeChunk> selectedChunkList) {
        StringBuilder builder = new StringBuilder();
        int length = 0;
        for (CourseKnowledgeChunk chunk : selectedChunkList) {
            String text = "\n【资料：" + chunk.getFileName() + "】\n" + chunk.getContent();
            if (length + text.length() > MAX_CONTEXT_LENGTH) {
                break;
            }
            builder.append(text);
            length += text.length();
        }
        return builder.toString();
    }

    private String limitContent(String content, int maxLength) {
        if (StrUtil.length(content) <= maxLength) {
            return content;
        }
        return StrUtil.sub(content, 0, maxLength);
    }

    /**
     * TODO 后续接入持久化 VectorStore 后，这里可以替换为 Embedding 检索。
     */
    private static class CourseKnowledgeChunk {

        private final String fileName;

        private final String content;

        private int score;

        private CourseKnowledgeChunk(String fileName, String content) {
            this.fileName = fileName;
            this.content = content;
        }

        private String getFileName() {
            return fileName;
        }

        private String getContent() {
            return content;
        }

        private int getScore() {
            return score;
        }

        private void setScore(int score) {
            this.score = score;
        }
    }
}
