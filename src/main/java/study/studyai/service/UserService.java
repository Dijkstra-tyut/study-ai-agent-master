package study.studyai.service;

import study.studyai.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import study.studyai.model.vo.LoginUserVO;

import javax.servlet.http.HttpServletRequest;

/**
* @author Lenovo
* @description 针对表【user(用户表 (统一管理所有角色))】的数据库操作Service
* @createDate 2026-06-08 17:20:38
*/
public interface UserService extends IService<User> {
    long userRegister(String username, String password, String checkPassword);
    
    LoginUserVO userLogin(String username, String password, HttpServletRequest request);

    LoginUserVO getLoginUserVO(User user);

    User getLoginUser(HttpServletRequest request);

    boolean userLogout(HttpServletRequest request);
}
