package study.studyai.service.impl;

import cn.hutool.core.util.StrUtil;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import study.studyai.common.BaseResponse;
import study.studyai.common.ErrorCode;
import study.studyai.common.ResultUtils;
import study.studyai.config.CosClientConfig;
import study.studyai.exception.BusinessException;
import study.studyai.manager.CosManager;
import study.studyai.manager.FileManager;
import study.studyai.model.entity.User;
import study.studyai.model.enums.FileUploadEnum;
import study.studyai.model.vo.FileUploadVO;
import study.studyai.service.FileService;
import study.studyai.service.UserService;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    @Resource
    private UserService userService;

    @Resource
    private CosManager cosManager;

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private FileManager fileManager;

    @Override
    public BaseResponse<String> uploadAvatar(MultipartFile multipartFile, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        FileUploadVO fileUploadVO = this.uploadFile(multipartFile, FileUploadEnum.AVATAR);
        User user = new User();
        user.setId(loginUser.getId());
        user.setAvatar(fileUploadVO.getFileUrl());
        boolean result = userService.updateById(user);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        return ResultUtils.success(fileUploadVO.getFileUrl());
    }

    @Override
    public FileUploadVO uploadFile(MultipartFile multipartFile, FileUploadEnum fileUploadEnum) {
        if (fileUploadEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        fileManager.validFile(multipartFile, fileUploadEnum);
        String suffix = fileManager.getFileSuffix(multipartFile);
        String key = String.format("%s/%s.%s", fileUploadEnum.getValue(), UUID.randomUUID(), suffix);
        File file = null;
        try {
            file = fileManager.transferToTempFile(multipartFile);
            cosManager.putObject(key, file);
            FileUploadVO fileUploadVO = new FileUploadVO();
            fileUploadVO.setFileName(multipartFile.getOriginalFilename());
            fileUploadVO.setFileKey(key);
            fileUploadVO.setFileUrl(getFileUrl(key));
            fileUploadVO.setFileType(suffix);
            fileUploadVO.setFileSize(multipartFile.getSize());
            return fileUploadVO;
        } finally {
            fileManager.deleteTempFile(file);
        }
    }

    @Override
    public COSObject downloadFile(String key) {
        if (StrUtil.isBlank(key)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return cosManager.getObject(key);
    }

    @Override
    public void downloadFileToResponse(String key, HttpServletResponse response) {
        COSObject cosObject = this.downloadFile(key);
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
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "下载失败");
        }
    }

    @Override
    public boolean deleteFile(String key) {
        if (StrUtil.isBlank(key)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        cosManager.deleteObject(key);
        return true;
    }

    private String getFileUrl(String key) {
        String host = cosClientConfig.getHost();
        if (StrUtil.isBlank(host)) {
            return key;
        }
        return host + "/" + key;
    }
}
