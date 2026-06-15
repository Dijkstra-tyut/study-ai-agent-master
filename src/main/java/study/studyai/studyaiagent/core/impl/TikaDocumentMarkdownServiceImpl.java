package study.studyai.studyaiagent.core.impl;

import cn.hutool.core.util.StrUtil;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import study.studyai.common.ErrorCode;
import study.studyai.exception.BusinessException;
import study.studyai.studyaiagent.core.DocumentMarkdownService;

import java.io.InputStream;

@Service
public class TikaDocumentMarkdownServiceImpl implements DocumentMarkdownService {

    @Override
    public String convertToMarkdown(MultipartFile multipartFile) {
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        try (InputStream inputStream = multipartFile.getInputStream()) {
            AutoDetectParser parser = new AutoDetectParser();
            BodyContentHandler handler = new BodyContentHandler(-1);
            Metadata metadata = new Metadata();
            ParseContext parseContext = new ParseContext();
            parser.parse(inputStream, handler, metadata, parseContext);
            String content = handler.toString();
            if (StrUtil.isBlank(content)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件内容为空");
            }
            return formatMarkdown(content);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "文件解析失败");
        }
    }

    private String formatMarkdown(String content) {
        String[] lines = content.replace("\r", "\n").split("\n");
        StringBuilder stringBuilder = new StringBuilder();
        for (String line : lines) {
            String text = StrUtil.trim(line);
            if (StrUtil.isBlank(text)) {
                continue;
            }
            stringBuilder.append(text).append("\n\n");
        }
        return stringBuilder.toString();
    }
}