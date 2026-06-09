package study.studyai.model.dto.user;

import lombok.Data;
import study.studyai.common.PageRequest;

import java.io.Serializable;

@Data
public class UserQueryRequest extends PageRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 账号
     */
    private String username;

    /**
     * 角色
     */
    private String role;
}
