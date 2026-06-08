package study.studyai.model.dto.user;

import lombok.Data;

@Data
public class UserRegisterRequest {

    private static final long serialVersionUID = 1L;
    /**
     * 账号
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 确认密码
     */
    private String checkPassword;
}
