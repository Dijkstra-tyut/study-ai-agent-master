package study.studyai.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import study.studyai.annotation.AuthCheck;
import study.studyai.common.BaseResponse;
import study.studyai.common.ErrorCode;
import study.studyai.common.ResultUtils;
import study.studyai.constant.UserConstant;
import study.studyai.exception.BusinessException;
import study.studyai.model.enums.FileUploadEnum;
import study.studyai.model.vo.FileUploadVO;
import study.studyai.service.FileService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/file")
public class FileController {

    @Resource
    private FileService fileService;

//    @PostMapping("/upload")
//    @AuthCheck(mustRole = UserConstant.TEACHER_ROLE)
//    public BaseResponse<FileUploadVO> uploadFile(@RequestPart("file") MultipartFile multipartFile, String biz) {
//        FileUploadEnum fileUploadEnum = FileUploadEnum.getEnumByValue(biz);
//        if (fileUploadEnum == null) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR);
//        }
//        if (FileUploadEnum.COURSE.equals(fileUploadEnum)) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR, "课程文件请使用课程文件上传接口");
//        }
//        FileUploadVO fileUploadVO = fileService.uploadFile(multipartFile, fileUploadEnum);
//        return ResultUtils.success(fileUploadVO);
//    }

    @PostMapping("/upload/avatar")
    @AuthCheck
    public BaseResponse<String> uploadAvatar(@RequestPart("file") MultipartFile multipartFile, HttpServletRequest request) {
        return fileService.uploadAvatar(multipartFile, request);
    }

//    @GetMapping("/download")
//    @AuthCheck
//    public void downloadFile(String key, HttpServletResponse response) {
//        checkNotCourseFile(key);
//        fileService.downloadFileToResponse(key, response);
//    }

//    @PostMapping("/delete")
//    @AuthCheck(mustRole = UserConstant.TEACHER_ROLE)
//    public BaseResponse<Boolean> deleteFile(String key) {
//        checkNotCourseFile(key);
//        boolean result = fileService.deleteFile(key);
//        return ResultUtils.success(result);
//    }

//    private void checkNotCourseFile(String key) {
//        if (key != null && key.startsWith(FileUploadEnum.COURSE.getValue() + "/")) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR, "课程文件请使用课程文件接口");
//        }
//    }
}
