package study.studyai.manager;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import study.studyai.common.ErrorCode;
import study.studyai.exception.BusinessException;
import study.studyai.model.enums.FileUploadEnum;

import java.io.File;
import java.util.Arrays;
import java.util.List;

@Component
public class FileManager {

    private static final List<String> IMAGE_SUFFIX_LIST = Arrays.asList("jpg", "jpeg", "png", "webp");

    private static final List<String> DOCUMENT_SUFFIX_LIST = Arrays.asList("pdf", "doc", "docx", "ppt", "pptx", "xls", "xlsx", "txt", "md");

    public void validFile(MultipartFile multipartFile, FileUploadEnum fileUploadEnum) {
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件不能为空");
        }
        String suffix = getFileSuffix(multipartFile);
        if (FileUploadEnum.AVATAR.equals(fileUploadEnum) && !IMAGE_SUFFIX_LIST.contains(suffix)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "头像格式错误");
        }
        if (FileUploadEnum.COURSE.equals(fileUploadEnum) && !DOCUMENT_SUFFIX_LIST.contains(suffix)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件格式错误");
        }
        // TODO 接入大模型后，在这里校验上传文件内容是否符合课程或知识库主题。
    }

    public File transferToTempFile(MultipartFile multipartFile) {
        String suffix = getFileSuffix(multipartFile);
        try {
            File file = File.createTempFile("cos-upload-", "." + suffix);
            multipartFile.transferTo(file);
            return file;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        }
    }

    public void deleteTempFile(File file) {
        if (file == null) {
            return;
        }
        FileUtil.del(file);
    }

    public String getFileSuffix(MultipartFile multipartFile) {
        String suffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        if (StrUtil.isBlank(suffix)) {
            return "tmp";
        }
        return suffix.toLowerCase();
    }
}
