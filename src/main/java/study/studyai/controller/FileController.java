package study.studyai.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import study.studyai.common.BaseResponse;
import study.studyai.common.ErrorCode;
import study.studyai.common.ResultUtils;
import study.studyai.config.CosClientConfig;
import study.studyai.exception.BusinessException;
import study.studyai.manager.CosManager;
import study.studyai.model.entity.User;
import study.studyai.service.UserService;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/file")
public class FileController {

    @Resource
    private CosManager cosManager;

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private UserService userService;

    @PostMapping("/upload")
    public BaseResponse<String> uploadFile(@RequestPart("file") MultipartFile multipartFile) {
        String fileUrl = uploadFileToCos(multipartFile, "file", false);
        return ResultUtils.success(fileUrl);
    }

    @PostMapping("/upload/avatar")
    public BaseResponse<String> uploadAvatar(@RequestPart("file") MultipartFile multipartFile, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        String fileUrl = uploadFileToCos(multipartFile, "avatar", true);
        User user = new User();
        user.setId(loginUser.getId());
        user.setAvatar(fileUrl);
        boolean result = userService.updateById(user);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        return ResultUtils.success(fileUrl);
    }

    @GetMapping("/download")
    public void downloadFile(String key, HttpServletResponse response) {
        if (StrUtil.isBlank(key)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        COSObject cosObject = cosManager.getObject(key);
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
        if (StrUtil.isBlank(key)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        cosManager.deleteObject(key);
        return ResultUtils.success(true);
    }

    private String uploadFileToCos(MultipartFile multipartFile, String biz, boolean checkImage) {
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件不能为空");
        }
        String originalFilename = multipartFile.getOriginalFilename();
        String suffix = FileUtil.getSuffix(originalFilename);
        if (StrUtil.isBlank(suffix)) {
            suffix = "tmp";
        }
        if (checkImage && !Arrays.asList("jpg", "jpeg", "png", "webp").contains(suffix.toLowerCase())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "头像格式错误");
        }
        File file = null;
        try {
            file = File.createTempFile("cos-upload-", "." + suffix);
            multipartFile.transferTo(file);
            String key = String.format("%s/%s.%s", biz, UUID.randomUUID(), suffix);
            cosManager.putObject(key, file);
            return getFileUrl(key);
        } catch (Exception e) {
            log.error("upload file error", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            if (file != null) {
                FileUtil.del(file);
            }
        }
    }

    private String getFileUrl(String key) {
        String host = cosClientConfig.getHost();
        if (StrUtil.isBlank(host)) {
            return key;
        }
        return host + "/" + key;
    }
}
