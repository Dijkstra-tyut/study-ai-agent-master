package study.studyai.service;

import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import study.studyai.common.BaseResponse;

import javax.servlet.http.HttpServletRequest;

public interface FileService {

    BaseResponse<String> uploadAvatar(@RequestPart("file") MultipartFile multipartFile, HttpServletRequest request);
}
