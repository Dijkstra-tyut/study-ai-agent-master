package study.studyai.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import study.studyai.common.BaseResponse;
import study.studyai.common.ErrorCode;
import study.studyai.common.ResultUtils;
import study.studyai.config.CosClientConfig;
import study.studyai.exception.BusinessException;
import study.studyai.manager.CosManager;
import study.studyai.model.entity.User;
import study.studyai.service.FileService;
import study.studyai.service.UserService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.Arrays;
import java.util.UUID;


@Service
public class FileServiceImpl implements FileService {

    @Resource
    private UserService userService;
    @Resource
    private CosManager cosManager;
    @Resource
    private CosClientConfig cosClientConfig;


    @Override
    public BaseResponse<String> uploadAvatar(MultipartFile multipartFile, HttpServletRequest request) {
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


    private String uploadFileToCos(MultipartFile multipartFile, String biz, boolean checkImage) {
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件不能为空");
        }
        // 校验文件后缀
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
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            // 临时文件清理
            this.deleteTempFile(file);
        }
    }

    private String getFileUrl(String key) {
        String host = cosClientConfig.getHost();
        if (StrUtil.isBlank(host)) {
            return key;
        }
        return host + "/" + key;
    }

    //TODO 文件校验 通过大模型校验上传的文件是否符合规则
    private void validFile(MultipartFile multipartFile) {

    }

    public void deleteTempFile(File file) {
        if (file == null) {
            return;
        }
        // 删除临时文件
        boolean deleteResult = file.delete();
    }
}
