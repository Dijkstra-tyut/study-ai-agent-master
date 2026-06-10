package study.studyai.controller;

import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import study.studyai.common.BaseResponse;
import study.studyai.common.ErrorCode;
import study.studyai.common.ResultUtils;
import study.studyai.exception.BusinessException;
import study.studyai.model.enums.FileUploadEnum;
import study.studyai.model.vo.FileUploadVO;
import study.studyai.service.FileService;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@RestController
@RequestMapping("/file")
public class FileController {

    @Resource
    private FileService fileService;

    @PostMapping("/upload")
    public BaseResponse<FileUploadVO> uploadFile(@RequestPart("file") MultipartFile multipartFile, String biz) {
        FileUploadEnum fileUploadEnum = FileUploadEnum.getEnumByValue(biz);
        if (fileUploadEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        FileUploadVO fileUploadVO = fileService.uploadFile(multipartFile, fileUploadEnum);
        return ResultUtils.success(fileUploadVO);
    }

    @PostMapping("/upload/avatar")
    public BaseResponse<String> uploadAvatar(@RequestPart("file") MultipartFile multipartFile, HttpServletRequest request) {
        return fileService.uploadAvatar(multipartFile, request);
    }

    @GetMapping("/download")
    public void downloadFile(String key, HttpServletResponse response) {
        COSObject cosObject = fileService.downloadFile(key);
        try (COSObjectInputStream cosObjectInputStream = cosObject.getObjectContent();
             ServletOutputStream outputStream = response.getOutputStream()) {
            String fileName = key.substring(key.lastIndexOf("/") + 1);
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()));
            byte[] buffer = new byte[1024];
            int len;
            while ((len = cosObjectInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
        } catch (Exception e) {
            log.error("download file error", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "下载失败");
        }
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteFile(String key) {
        boolean result = fileService.deleteFile(key);
        return ResultUtils.success(result);
    }
}
