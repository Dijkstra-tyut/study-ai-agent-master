package study.studyai.model.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class FileUploadVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * COS对象key
     */
    private String fileKey;

    /**
     * 文件访问地址
     */
    private String fileUrl;

    /**
     * 文件类型
     */
    private String fileType;

    /**
     * 文件大小
     */
    private Long fileSize;
}
