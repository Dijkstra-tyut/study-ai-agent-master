package study.studyai.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import study.studyai.common.BaseResponse;
import study.studyai.common.ErrorCode;
import study.studyai.common.ResultUtils;
import study.studyai.exception.BusinessException;
import study.studyai.model.enums.FileUploadEnum;
import study.studyai.model.vo.FileUploadVO;
import study.studyai.service.FileService;
import study.studyai.service.UserService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/file")
public class FileController {

    @Resource
    private FileService fileService;

    @Resource
    private UserService userService;

    @PostMapping("/upload")
    public BaseResponse<FileUploadVO> uploadFile(@RequestPart("file") MultipartFile multipartFile, String biz, HttpServletRequest request) {
        userService.getLoginUser(request);
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
    public void downloadFile(String key, HttpServletRequest request, HttpServletResponse response) {
        userService.getLoginUser(request);
        fileService.downloadFileToResponse(key, response);
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteFile(String key, HttpServletRequest request) {
        userService.getLoginUser(request);
        boolean result = fileService.deleteFile(key);
        return ResultUtils.success(result);
    }
}
