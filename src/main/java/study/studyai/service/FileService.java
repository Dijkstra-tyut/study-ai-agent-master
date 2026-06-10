package study.studyai.service;

import com.qcloud.cos.model.COSObject;
import org.springframework.web.multipart.MultipartFile;
import study.studyai.common.BaseResponse;
import study.studyai.model.enums.FileUploadEnum;
import study.studyai.model.vo.FileUploadVO;

import javax.servlet.http.HttpServletRequest;

public interface FileService {

    BaseResponse<String> uploadAvatar(MultipartFile multipartFile, HttpServletRequest request);

    FileUploadVO uploadFile(MultipartFile multipartFile, FileUploadEnum fileUploadEnum);

    COSObject downloadFile(String key);

    boolean deleteFile(String key);
}
