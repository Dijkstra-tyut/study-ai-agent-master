package study.studyai.manager;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import study.studyai.common.ErrorCode;
import study.studyai.config.CosClientConfig;
import study.studyai.exception.BusinessException;

import javax.annotation.Resource;
import java.io.File;

@Component
public class CosManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Autowired(required = false)
    private COSClient cosClient;

    /**
     * 上传对象
     *
     * @param key  唯一键
     * @param file 文件
     */
    public PutObjectResult putObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key,
                file);
        return getCosClient().putObject(putObjectRequest);
    }

    /**
     * 下载对象
     *
     * @param key 唯一键
     */
    public COSObject getObject(String key) {
        GetObjectRequest getObjectRequest = new GetObjectRequest(cosClientConfig.getBucket(), key);
        return getCosClient().getObject(getObjectRequest);
    }

    /**
     * 删除对象
     *
     * @param key 唯一键
     */
    public void deleteObject(String key) {
        getCosClient().deleteObject(cosClientConfig.getBucket(), key);
    }

    private COSClient getCosClient() {
        if (cosClient == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "COS配置未完成");
        }
        return cosClient;
    }
}
