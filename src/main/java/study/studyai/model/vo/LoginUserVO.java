package study.studyai.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 已登录用户视图（脱敏）
 */
@Data
public class LoginUserVO implements Serializable {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 账号
     */
    private String username;

    /**
     * 角色: student/teacher/admin
     */
    private String role;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    private static final long serialVersionUID = 1L;
}
